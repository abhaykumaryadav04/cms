package com.a4b.cms.club.dto;

import com.a4b.cms.club.entity.ClubStatus;

import jakarta.validation.constraints.NotNull;

public record UpdateClubStatus(
      @NotNull(message = "Club status is required")
        ClubStatus status
) {

}
