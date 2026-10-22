package com.a4b.cms.membership.dto;

import jakarta.validation.constraints.Size;

public record JoinClubRequest(
      @Size(
            max = 1000,
            message = "Membership request message cannot exceed 1000 characters"
        )
        String message
) {

}
