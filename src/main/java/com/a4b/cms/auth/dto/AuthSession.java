package com.a4b.cms.auth.dto;

import com.a4b.cms.user.dto.AuthResponse;

public record AuthSession(
      AuthResponse response,
        String refreshToken
) {

}
