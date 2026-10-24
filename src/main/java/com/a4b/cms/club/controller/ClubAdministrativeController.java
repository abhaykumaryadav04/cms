package com.a4b.cms.club.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.a4b.cms.club.dto.AssignFacultyAdvisorRequest;
import com.a4b.cms.club.dto.AssignclubRoleRequest;
import com.a4b.cms.club.dto.ClubLeadershipResponse;
import com.a4b.cms.club.dto.ClubResponse;
import com.a4b.cms.club.service.ClubAdministrativeService;

import jakarta.validation.Valid;

@RestController 
@RequestMapping("/api/v1/admin/clubs")
public class ClubAdministrativeController {
    private final ClubAdministrativeService clubAdministrativeService;
    public ClubAdministrativeController(ClubAdministrativeService clubAdministrativeService){
        this.clubAdministrativeService=clubAdministrativeService;
    }

     @PutMapping("/{clubId}/members/{userId}/role")
    public ResponseEntity<ClubLeadershipResponse> assignClubRole(
            @PathVariable UUID clubId,
            @PathVariable UUID userId,
            @Valid @RequestBody AssignclubRoleRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {

        ClubLeadershipResponse response = clubAdministrativeService.assignClubRole(clubId,userId,userDetails.getUsername(),request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{clubId}/faculty-advisor")
    public ResponseEntity<ClubResponse> assignFacultyAdvisor(
            @PathVariable UUID clubId,
            @RequestBody AssignFacultyAdvisorRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {

        ClubResponse response =clubAdministrativeService.assignFacultyAdvisor(clubId,userDetails.getUsername(),request);

        return ResponseEntity.ok(response);
    }

}
