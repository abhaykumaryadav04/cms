package com.a4b.cms.user.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.a4b.cms.user.dto.UpdateUserProfileRequest;
import com.a4b.cms.user.dto.UserProfileResponse;
import com.a4b.cms.user.service.UserService;

import jakarta.validation.Valid;

@RestController 
@RequestMapping ("/api/v1/users")
public class UserController {

 private final UserService userService;

 public UserController(UserService userService){
    this.userService=userService;
 }
   @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMyProfile( @AuthenticationPrincipal UserDetails userDetails) {

        UserProfileResponse profile = userService.getMyProfile(userDetails.getUsername());

        return ResponseEntity.ok(profile);
 }
 @PatchMapping ("/me")
 public  ResponseEntity<UserProfileResponse> updateUserProfile(@AuthenticationPrincipal  UserDetails userDetails,@RequestBody @Valid  UpdateUserProfileRequest request){
  UserProfileResponse profile=userService.updateUserProfile(userDetails.getUsername(), request);
  return ResponseEntity.status(HttpStatus.ACCEPTED).body(profile);
 }

}
