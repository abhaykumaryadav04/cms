package com.a4b.cms.club.dto;

import jakarta.validation.constraints.Size;

public record UpdateClubRequest(
       @Size(
                max = 100,
                message = "Club name cannot exceed 100 characters"
        )
        String name,

        @Size(
                max = 5000,
                message = "Description cannot exceed 5000 characters"
        )
        String description,

        @Size(
                max = 100,
                message = "Category cannot exceed 100 characters"
        )
        String category,

        @Size(
                max = 500,
                message = "Logo URL cannot exceed 500 characters"
        )
        String logoUrl

) {

}
