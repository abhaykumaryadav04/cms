package com.a4b.cms.user.service;

import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.a4b.cms.common.exception.ApiException;
import com.a4b.cms.user.dto.UpdateUserProfileRequest;
import com.a4b.cms.user.dto.UserProfileResponse;
import com.a4b.cms.user.entity.User;
import com.a4b.cms.user.repo.UserRepo;

@Service
public class UserService {
 
    private  final UserRepo userRepo;
    public UserService(UserRepo userRepo){
        this.userRepo=userRepo;
    }
    @Transactional (readOnly = true)
    public UserProfileResponse getMyProfile(String email){

    User user=findUserByEmail(email);
    return toProfileResponse(user);
    }
    @Transactional 
    public UserProfileResponse updateUserProfile(String email,UpdateUserProfileRequest request){
    User user=findUserByEmail(email);
      boolean hasChanges =request.fullName() != null || request.department() != null|| request.yearOfStudy() != null|| request.phone() != null;

        if (!hasChanges) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "EMPTY_PROFILE_UPDATE",
                    "Provide at least one field to update.");
        }
         if (request.fullName() != null) {

            if (request.fullName().isBlank()) {
                throw new ApiException(
                        HttpStatus.BAD_REQUEST,
                        "INVALID_FULL_NAME",
                        "Full name cannot be blank."
                );
            }

            user.setFullName( request.fullName().trim());
        }
        if (request.department() != null) {

            String department = request.department().trim();

            user.setDepartment(
                    department.isEmpty() ? null : department
            );
        }
        if (request.yearOfStudy() != null) {

            user.setYearOfStudy(
                    request.yearOfStudy()
            );
        }
        if (request.phone() != null) {
            String phone = request.phone().trim();
            user.setPhone(phone.isEmpty() ? null : phone);
        }

        User updatedUser = userRepo.save(user);

        return toProfileResponse(updatedUser);

    }
     private User findUserByEmail(String email) {

        String normalizedEmail = email.trim()
                .toLowerCase(Locale.ROOT);

        return userRepo.findByEmailIgnoreCase(normalizedEmail).orElseThrow(() ->
                        new ApiException(
                                HttpStatus.NOT_FOUND,
                                "USER_NOT_FOUND",
                                "The requested user account was not found."));
    }
      private UserProfileResponse toProfileResponse(User user) {

        return new UserProfileResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getEnrollNumber(),
                user.getDepartment(),
                user.getYearOfStudy(),
                user.getPhone(),
                user.getSystemRole(),
                user.getAccountStatus(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
