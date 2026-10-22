package com.a4b.cms.membership.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.a4b.cms.membership.dto.JoinClubRequest;
import com.a4b.cms.membership.dto.MembershipResponse;
import com.a4b.cms.membership.dto.ReviewMembershipRequest;
import com.a4b.cms.membership.entity.MembershipStatus;
import com.a4b.cms.membership.service.ClubMembershipService;

import jakarta.validation.Valid;

@RestController 
public class ClubMembershipController {
     private final ClubMembershipService membershipService;

    public ClubMembershipController(
            ClubMembershipService membershipService
    ) {
        this.membershipService = membershipService;
    }
     @PostMapping("/api/v1/clubs/{clubId}/join-requests")
    public ResponseEntity<MembershipResponse> requestMembership(
            @PathVariable UUID clubId,
            @Valid @RequestBody JoinClubRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {

        MembershipResponse response = membershipService.requestMembership(
                        clubId,
                        userDetails.getUsername(),
                        request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
      @GetMapping("/api/v1/clubs/{clubId}/join-requests")
    public ResponseEntity<Page<MembershipResponse>> getRequests(
            @PathVariable UUID clubId,
            @RequestParam(
                    defaultValue = "PENDING"
            ) MembershipStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails
    ) {

        return ResponseEntity.ok( membershipService.getClubMembership( clubId,status,
                        userDetails.getUsername(),page,size));

    }
    @PatchMapping("/api/v1/clubs/{clubId}/join-requests/{requestId}")
    public ResponseEntity<MembershipResponse> reviewRequest(
            @PathVariable UUID clubId,
            @PathVariable UUID requestId,
            @Valid @RequestBody ReviewMembershipRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {

        return ResponseEntity.ok(
                membershipService.reviewRequest(
                        clubId,
                        requestId,
                        userDetails.getUsername(),
                        request
                )
        );
    }
     @DeleteMapping("/api/v1/clubs/{clubId}/membership/me")
    public ResponseEntity<Void> leaveClub(
            @PathVariable UUID clubId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {

        membershipService.leaveClub(
                clubId,
                userDetails.getUsername()
        );

        return ResponseEntity.noContent().build();
    }
}
