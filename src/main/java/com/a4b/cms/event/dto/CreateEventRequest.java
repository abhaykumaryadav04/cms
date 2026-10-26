package com.a4b.cms.event.dto;

import java.time.Instant;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateEventRequest(
      @NotBlank(message = "Event title is required")
        @Size(max = 150, message = "Title cannot exceed 150 characters")
        String title,
        @Size(max = 10000, message = "Description is too long")
        String description,
        @NotBlank(message = "Venue is required")
        @Size(max = 250, message = "Venue cannot exceed 250 characters")
        String venue,
        @NotNull(message = "Event start time is required")
        @Future(message = "Event start time must be in the future")
        Instant startsAt,
        @NotNull(message = "Event end time is required")
        Instant endsAt,
        @Positive(message = "Capacity must be greater than zero")
        Integer capacity,
        @NotNull(message = "Registration deadline is required")
        Instant registrationDeadline

) {

}
