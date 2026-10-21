package com.a4b.cms.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank(message = "Full name is required")
        @Size(max = 100, message = "Full name cannot exceed 100 characters")
        String fullName,

        @NotBlank(message = "Email is required")
        @Email(message = "Enter a valid email address")
        @Size(max = 254, message = "Email is too long")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 72,
              message = "Password must contain 8 to 72 characters")
        String password,

        @Size(max = 30, message = "Roll number is too long")
        String rollNumber,

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
