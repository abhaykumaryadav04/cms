package com.a4b.cms.membership.dto;

import java.time.Instant;
import java.util.UUID;

import com.a4b.cms.membership.entity.ClubRole;
import com.a4b.cms.membership.entity.MembershipStatus;

public record MembershipResponse(
     UUID requestId,
        UUID clubId,
        String clubName,
        UUID userId,
        String userName,
        ClubRole clubRole,
        MembershipStatus status,
        String message,
        String reviewReason,
        Instant requestedAt,
        UUID reviewedById,
        Instant reviewedAt,
        Instant joinedAt
) {

}
