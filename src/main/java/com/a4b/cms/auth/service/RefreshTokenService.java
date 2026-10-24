package com.a4b.cms.auth.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.a4b.cms.auth.dto.RefreshTokenRotation;
import com.a4b.cms.auth.entity.RefreshToken;
import com.a4b.cms.auth.repo.RefreshTokenRepo;
import com.a4b.cms.common.exception.ApiException;
import com.a4b.cms.user.entity.AccountStatus;
import com.a4b.cms.user.entity.User;

@Service 
public class RefreshTokenService {

    private static final SecureRandom SECURE_RANDOM =
            new SecureRandom();

    private final RefreshTokenRepo tokenRepository;

    private final long expirationSeconds;

    public RefreshTokenService(
            RefreshTokenRepo tokenRepository,
            @Value("${app.auth.refresh-token-expiration-seconds:604800}")
            long expirationSeconds
    ) {
        this.tokenRepository = tokenRepository;
        this.expirationSeconds = expirationSeconds;
    }
    @Transactional 
      public String issueForUser(User user) {

        String rawToken = generateRandomToken();

        RefreshToken refreshToken = new RefreshToken();

        refreshToken.setUser(user);
        refreshToken.setTokenHash(hashToken(rawToken));
        refreshToken.setExpiresAt(Instant.now().plusSeconds(expirationSeconds));

        tokenRepository.save(refreshToken);

        return rawToken;
    }
    @Transactional 
    public RefreshTokenRotation rotate(String rawToken){
         if (rawToken == null || rawToken.isBlank()) {
            throw invalidRefreshToken();
        }
         String tokenHash = hashToken(rawToken);
        RefreshToken oldToken = tokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(this::invalidRefreshToken);

        Instant now = Instant.now();
        if (oldToken.getRevokedAt() != null|| !oldToken.getExpiresAt().isAfter(now)) {
            throw invalidRefreshToken();
        }
        User user = oldToken.getUser();
        if (user.getAccountStatus() != AccountStatus.ACTIVE) {
            throw invalidRefreshToken();
        }
         oldToken.setRevokedAt(now);
        tokenRepository.save(oldToken);
          String replacementToken = generateRandomToken();
          RefreshToken refreshToken=RefreshToken.builder().createdAt(now)
                                                          .expiresAt( now.plusSeconds(expirationSeconds))
                                                          .user(user)
                                                          .tokenHash(hashToken(replacementToken))
                                                          .build();
        tokenRepository.save(refreshToken);
        return new RefreshTokenRotation(user, replacementToken);
                                                          
    }
    @Transactional
    public void revoke(String rawToken) {

        if (rawToken == null || rawToken.isBlank()) {
            return;
        }

        String tokenHash = hashToken(rawToken);

        tokenRepository.findByTokenHash(tokenHash)
                .ifPresent(token -> {

                    if (token.getRevokedAt() == null) {
                        token.setRevokedAt(Instant.now());
                        tokenRepository.save(token);
                    }
                });
    }
     public long getExpirationSeconds() {
        return expirationSeconds;
    }

    private String generateRandomToken() {

        byte[] randomBytes = new byte[32];

        SECURE_RANDOM.nextBytes(randomBytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);
    }

    private String hashToken(String rawToken) {

        try {
            MessageDigest digest =MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8) );
            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(
                    "SHA-256 is unavailable.",
                    ex
            );
        }
    }

    private ApiException invalidRefreshToken() {

        return new ApiException(
                HttpStatus.UNAUTHORIZED,
                "INVALID_REFRESH_TOKEN",
                "The refresh token is invalid, expired, or revoked."
        );
    }
}
