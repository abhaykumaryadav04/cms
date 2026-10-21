package com.a4b.cms.user.dto;

public record AuthResponse(
       String accessToken,
        String tokenType,
        long expiresIn,
        UserProfileResponse user
) {

}
