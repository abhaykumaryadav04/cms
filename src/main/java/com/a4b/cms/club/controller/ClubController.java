package com.a4b.cms.club.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.a4b.cms.club.dto.ClubResponse;
import com.a4b.cms.club.dto.CreateClubRequest;
import com.a4b.cms.club.service.ClubService;

import jakarta.validation.Valid;

@RestController 
@RequestMapping ("/api/v1/clubs")
public class ClubController {

    private  final ClubService clubService;

     public ClubController(ClubService clubService) {
        this.clubService = clubService;
    }
    @PostMapping
    public ResponseEntity<ClubResponse> createClub(
            @Valid @RequestBody CreateClubRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {

        ClubResponse response = clubService.createClub( userDetails.getUsername(), request );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
    @GetMapping
    public ResponseEntity<Page<ClubResponse>> getActiveClubs(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {

        Page<ClubResponse> clubs =clubService.searchClub(search, category, page, size);

        return ResponseEntity.ok(clubs);
    }
     @GetMapping("/{clubId}")
    public ResponseEntity<ClubResponse> getClub(
            @PathVariable UUID clubId
    ) {

        return ResponseEntity.ok(
                clubService.getActiveClub(clubId)
        );
    }
}
