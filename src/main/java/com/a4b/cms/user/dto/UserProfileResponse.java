package com.a4b.cms.user.dto;

import java.time.Instant;
import java.util.UUID;

import com.a4b.cms.user.entity.AccountStatus;
import com.a4b.cms.user.entity.SystemRole;

public record UserProfileResponse(
     UUID id,
        String fullName,
        String email,
        String rollNumber,
        String department,
        Integer yearOfStudy,
        String phone,
        SystemRole systemRole,
        AccountStatus accountStatus,
        Instant createdAt,
        Instant updatedAt
) {

}
