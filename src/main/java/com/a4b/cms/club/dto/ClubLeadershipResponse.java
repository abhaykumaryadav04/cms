package com.a4b.cms.club.dto;

import java.time.Instant;
import java.util.UUID;

import com.a4b.cms.membership.entity.ClubRole;
import com.a4b.cms.membership.entity.MembershipStatus;

public record ClubLeadershipResponse(
     UUID clubId,
        String clubName,
        UUID userId,
        String userName,
        ClubRole clubRole,
        MembershipStatus membershipStatus,
        Instant updatedAt
) {

}
