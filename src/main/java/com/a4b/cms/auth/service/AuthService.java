package com.a4b.cms.auth.service;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.a4b.cms.auth.dto.AuthSession;
import com.a4b.cms.auth.dto.LoginRequest;
import com.a4b.cms.auth.dto.RefreshTokenRotation;
import com.a4b.cms.auth.dto.RegisterRequest;
import com.a4b.cms.auth.security.JwtService;
import com.a4b.cms.common.exception.ApiException;
import com.a4b.cms.user.dto.AuthResponse;
import com.a4b.cms.user.dto.UserProfileResponse;
import com.a4b.cms.user.entity.AccountStatus;
import com.a4b.cms.user.entity.SystemRole;
import com.a4b.cms.user.entity.User;
import com.a4b.cms.user.repo.UserRepo;

@Service 
public class AuthService {
    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    public  AuthService(UserRepo userRepo,JwtService jwtService,PasswordEncoder passwordEncoder,
        RefreshTokenService refreshTokenService
    ){
        this.passwordEncoder=passwordEncoder;
        this.jwtService=jwtService;
        this.userRepo=userRepo;
        this.refreshTokenService=refreshTokenService;
    }
    @Transactional 
    public  UserProfileResponse register(RegisterRequest request){
        String email=request.email().trim().toLowerCase(Locale.ROOT);
         if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "PASSWORD_TOO_LONG",
                    "The encoded password must not exceed 72 bytes."
            );
        }
        if(userRepo.existsByEmailIgnoreCase(email)){
             throw new ApiException(
                    HttpStatus.CONFLICT,
                    "EMAIL_ALREADY_EXISTS",
                    "An account with this email already exists."
            );
        }
        String rollNumber=request.rollNumber().toUpperCase(Locale.ROOT);
        if (rollNumber != null) {
            rollNumber = rollNumber.toUpperCase(Locale.ROOT);
            if (userRepo.existsByEnrollNumberIgnoreCase(rollNumber )) {
                throw new ApiException(
                        HttpStatus.CONFLICT,
                        "ROLL_NUMBER_ALREADY_EXISTS",
                        "An account with this roll number already exists."
                );
            }
        }
        User user=User.builder().department(request.department())
                                 .email(email)
                                 .enrollNumber(normalizeOptional(rollNumber))
                                 .fullName(request.fullName().trim())
                                 .passwordHash(passwordEncoder.encode(request.password()))
                                 .phone(normalizeOptional(request.phone()))
                                 .systemRole(SystemRole.STUDENT)
                                 .yearOfStudy(request.yearOfStudy())
                                 .build();
        User saved =userRepo.save(user);
         return toProfileResponse(saved);
    }
      @Transactional(readOnly = true)
    public AuthSession login(
            LoginRequest request
    ) {

        String email = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        User user = userRepo.findByEmailIgnoreCase(email)
                .orElseThrow(() -> invalidCredentials()  );

        if (user.getAccountStatus() != AccountStatus.ACTIVE) {
            throw invalidCredentials();
        }

        if (!passwordEncoder.matches(request.password(),user.getPasswordHash())) {
            throw invalidCredentials();
        }

     String accessToken =jwtService.generateToken(user);

        String refreshToken =refreshTokenService.issueForUser(user);

      AuthResponse response = new AuthResponse(
        accessToken,
        "Bearer",
        jwtService.getExpirationSeconds(),
        toProfileResponse(user)
        );

     return new AuthSession(
        response,
        refreshToken);
    }
 @Transactional
public AuthSession refresh(String rawRefreshToken) {

    RefreshTokenRotation rotation =refreshTokenService.rotate(rawRefreshToken);
    User user = rotation.user();
    String accessToken =jwtService.generateToken(user);

    AuthResponse response = new AuthResponse(
            accessToken,
            "Bearer",
            jwtService.getExpirationSeconds(),
            toProfileResponse(user)
    );

    return new AuthSession(
            response,
            rotation.rawToken()
    );
}
public void logout(String rawRefreshToken) {
    refreshTokenService.revoke(rawRefreshToken);
}


    private ApiException invalidCredentials() {

        return new ApiException(
                HttpStatus.UNAUTHORIZED,
                "INVALID_CREDENTIALS",
                "Invalid email or password."
        );
    }

    private String normalizeOptional(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private UserProfileResponse toProfileResponse(User user) {

        return new UserProfileResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getEnrollNumber(),
                user.getDepartment(),
                user.getYearOfStudy(),
                user.getPhone(),
                user.getSystemRole(),
                user.getAccountStatus(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

}
