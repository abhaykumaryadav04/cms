package com.a4b.cms.user.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateUserProfileRequest(
     @Size(max = 100, message = "Full name cannot exceed 100 characters")
        String fullName,

        @Size(max = 100, message = "Department name is too long")
        String department,

        @Min(value = 1, message = "Year of study must be at least 1")
        @Max(value = 8, message = "Year of study cannot exceed 8")
        Integer yearOfStudy,

        @Pattern(
            regexp = "^$|^\\+?[0-9]{7,15}$",
            message = "Enter a valid phone number"
        )
        String phone
) {

}
