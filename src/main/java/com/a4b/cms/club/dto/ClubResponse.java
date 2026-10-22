package com.a4b.cms.club.dto;

import java.time.Instant;
import java.util.UUID;

import com.a4b.cms.club.entity.ClubStatus;

public record ClubResponse(
        UUID id,
        String name,
        String slug,
        String description,
        String category,
        String logoUrl,
        ClubStatus status,
        UUID createdById,
        UUID facultyAdvisorId,
        Instant createdAt,
        Instant updatedAt
) {

}
