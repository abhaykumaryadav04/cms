package com.a4b.cms.event.dto;

import java.time.Instant;
import java.util.UUID;

import com.a4b.cms.event.entity.EventStatus;

public record EventResponse(
     UUID id,

        UUID clubId,
        String clubName,

        UUID createdById,
        String createdByName,

        String title,
        String description,
        String venue,

        Instant startsAt,
        Instant endsAt,

        Integer capacity,
        Instant registrationDeadline,

        EventStatus status,

        UUID approvedById,
        Instant approvedAt,

        String cancellationReason,

        Instant createdAt,
        Instant updatedAt
) {

}
