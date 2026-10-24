package com.a4b.cms.auth.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.a4b.cms.auth.dto.AuthSession;
import com.a4b.cms.auth.dto.LoginRequest;
import com.a4b.cms.auth.dto.RegisterRequest;
import com.a4b.cms.auth.service.AuthService;
import com.a4b.cms.user.dto.AuthResponse;
import com.a4b.cms.user.dto.UserProfileResponse;

import jakarta.validation.Valid;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController 
@RequestMapping ("/api/v1/auth")
public class AuthController {

   private static final String REFRESH_COOKIE_NAME = "ccms_refresh";

    private static final String REFRESH_COOKIE_PATH = "/api/v1/auth";

    private final AuthService authService;
    private final long refreshTokenExpirationSeconds;
    private final boolean refreshCookieSecure;

    public AuthController(
            AuthService authService,
            @Value("${app.auth.refresh-token-expiration-seconds:604800}")
            long refreshTokenExpirationSeconds,
            @Value("${app.auth.refresh-cookie-secure:false}")
            boolean refreshCookieSecure
    ) {
        this.authService = authService;
        this.refreshTokenExpirationSeconds =
                refreshTokenExpirationSeconds;
        this.refreshCookieSecure = refreshCookieSecure;
    }

    @PostMapping("/register")
    public ResponseEntity<UserProfileResponse> register(@RequestBody @Valid RegisterRequest  request) {
         UserProfileResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
        
    }
    @PostMapping ("/login")
    public  ResponseEntity<AuthResponse> login(@RequestBody @Valid  LoginRequest request){
       AuthSession session = authService.login(request);
        return ResponseEntity.ok().header(
            HttpHeaders.SET_COOKIE, createRefreshCookie(session.refreshToken() ).toString())
                .body(session.response());
    }
      @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @CookieValue(
                    name = REFRESH_COOKIE_NAME,
                    required = false
            )
            String refreshToken
    ) {

        AuthSession session = authService.refresh(refreshToken);
        return ResponseEntity.ok() .header( HttpHeaders.SET_COOKIE, createRefreshCookie(
                                session.refreshToken()).toString()).body(session.response());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(
                    name = REFRESH_COOKIE_NAME,
                    required = false
            )
            String refreshToken
    ) {

        authService.logout(refreshToken);

        return ResponseEntity.noContent() .header(
                        HttpHeaders.SET_COOKIE,
                        clearRefreshCookie().toString()).build();
    }

    private ResponseCookie createRefreshCookie(
            String refreshToken
    ) {

        return ResponseCookie
                .from(REFRESH_COOKIE_NAME, refreshToken)
                .httpOnly(true)
                .secure(refreshCookieSecure)
                .path(REFRESH_COOKIE_PATH)
                .sameSite("Strict")
                .maxAge(Duration.ofSeconds(
                        refreshTokenExpirationSeconds
                ))
                .build();
    }

    private ResponseCookie clearRefreshCookie() {

        return ResponseCookie
                .from(REFRESH_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(refreshCookieSecure)
                .path(REFRESH_COOKIE_PATH)
                .sameSite("Strict")
                .maxAge(Duration.ZERO)
                .build();
    }
    

}
