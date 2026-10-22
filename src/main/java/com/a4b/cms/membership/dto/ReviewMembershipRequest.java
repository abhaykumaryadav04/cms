package com.a4b.cms.membership.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReviewMembershipRequest(

        @NotNull(message = "Decision is required")
        Decision decision,

        @Size(
            max = 1000,
            message = "Review reason cannot exceed 1000 characters"
        )
        String reason

) {
    public enum Decision {
        APPROVE,
        REJECT
    }
}
