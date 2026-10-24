package com.a4b.cms.auth.dto;

import com.a4b.cms.user.entity.User;

public record RefreshTokenRotation(
     User user,
        String rawToken
) {
} 
