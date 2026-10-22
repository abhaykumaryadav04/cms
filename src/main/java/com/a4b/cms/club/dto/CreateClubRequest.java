package com.a4b.cms.club.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateClubRequest(
    @NotBlank(message = "Club name is required")
        @Size(max = 100, message = "Club name cannot exceed 100 characters")
        String name,

        @Size(max = 5000, message = "Description is too long")
        String description,

        @Size(max = 100, message = "Category cannot exceed 100 characters")
        String category,

        @Size(max = 500, message = "Logo URL is too long")
        String logoUrl
) {

}
