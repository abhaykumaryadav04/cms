package com.a4b.cms.club.dto;

import jakarta.validation.constraints.NotNull;

public record AssignclubRoleRequest(

        @NotNull(message = "Club role is required")
        ClubLeadershipRole role

) {
    public enum ClubLeadershipRole {
        MEMBER,
        PRESIDENT,
        SECRETARY
    }
}
