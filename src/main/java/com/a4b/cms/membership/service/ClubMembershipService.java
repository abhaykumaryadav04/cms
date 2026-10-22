package com.a4b.cms.membership.service;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.a4b.cms.club.entity.Club;
import com.a4b.cms.club.entity.ClubStatus;
import com.a4b.cms.club.repo.ClubRepo;
import com.a4b.cms.common.exception.ApiException;
import com.a4b.cms.membership.dto.JoinClubRequest;
import com.a4b.cms.membership.dto.MembershipResponse;
import com.a4b.cms.membership.dto.ReviewMembershipRequest;
import com.a4b.cms.membership.entity.ClubMembership;
import com.a4b.cms.membership.entity.ClubRole;
import com.a4b.cms.membership.entity.MembershipStatus;
import com.a4b.cms.membership.repo.ClubMembershipRepo;
import com.a4b.cms.user.entity.SystemRole;
import com.a4b.cms.user.entity.User;
import com.a4b.cms.user.repo.UserRepo;

@Service 
public class ClubMembershipService {
    private final UserRepo userRepo;
    private final ClubMembershipRepo clubMembershipRepo;
    private final ClubRepo  clubRepo;
    public ClubMembershipService(UserRepo userRepo,ClubRepo clubRepo,ClubMembershipRepo clubMembershipRepo){
        this.clubRepo=clubRepo;
        this.userRepo=userRepo;
        this.clubMembershipRepo=clubMembershipRepo;
    }
    @Transactional
    public MembershipResponse requestMembership(
            UUID clubId,
            String email,
            JoinClubRequest request
    ) {

        User user = findUser(email);

        if (user.getSystemRole() != SystemRole.STUDENT) {
            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "STUDENT_ONLY",
                    "Only student accounts can request club membership."
            );
        }

        Club club = findActiveClub(clubId);

        ClubMembership membership =clubMembershipRepo
                        .findByUser_IdAndClub_Id(
                                user.getId(),
                                club.getId()
                        )
                        .orElse(null);

        if (membership != null
                && (membership.getStatus() == MembershipStatus.PENDING
                || membership.getStatus() == MembershipStatus.ACTIVE)) {

            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "MEMBERSHIP_REQUEST_EXISTS",
                    "A pending request or active membership already exists."
            );
        }
        if (membership == null) {
            membership = new ClubMembership();

            membership.setUser(user);
            membership.setClub(club);
            membership.setClubRole(ClubRole.MEMBER);
        }

        membership.setStatus(MembershipStatus.PENDING);
        membership.setRequestMessage(normalizeOptional(request.message()));

        membership.setRequestedAt(Instant.now());
        membership.setReviewedBy(null);
        membership.setReviewedAt(null);
        membership.setReviewReason(null);
        membership.setJoinedAt(null);

        ClubMembership saved = clubMembershipRepo.save(membership);

        return toResponse(saved);
    }

   @Transactional (readOnly = true)
   public Page<MembershipResponse> getMyMembership(String email,
            int page,
            int size){

        validatePagination(page, size);
        User user = findUser(email);
         Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "requestedAt")
        );

        return clubMembershipRepo.findByUser_Id(user.getId(), pageable)
                .map(this::toResponse);
            }
    @Transactional (readOnly = true)
    public Page<MembershipResponse> getClubMembership(  UUID clubId,
            MembershipStatus status,
            String email,
            int page,
            int size){
                 validatePagination(page, size);

        User actor = findUser(email);
        Club club = findClub(clubId);

        requireClubReviewer(actor, club);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.ASC, "requestedAt")
        );

        return clubMembershipRepo
                .findByClub_IdAndStatus(
                        clubId,
                        status,
                        pageable
                )
                .map(this::toResponse);
            }
    @Transactional 
    public   MembershipResponse reviewRequest( UUID clubId,
            UUID requestId,
            String email,
            ReviewMembershipRequest request){

         User reviewer = findUser(email);
        Club club = findClub(clubId);

        requireClubReviewer(reviewer, club);

        ClubMembership membership =clubMembershipRepo.findById(requestId)
                        .orElseThrow(() -> new ApiException(
                                HttpStatus.NOT_FOUND,
                                "MEMBERSHIP_REQUEST_NOT_FOUND",
                                "The membership request was not found." ));
            if (membership.getUser().getId().equals(reviewer.getId())) {
            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "SELF_REVIEW_NOT_ALLOWED",
                    "You cannot review your own membership request."
            );
        }
         if (membership.getStatus() != MembershipStatus.PENDING) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "REQUEST_ALREADY_REVIEWED",
                    "Only pending membership requests can be reviewed."
            );
        }
        membership.setReviewedBy(reviewer);
        membership.setReviewedAt(Instant.now());
        membership.setReviewReason(normalizeOptional(request.reason()));

        if (request.decision()== ReviewMembershipRequest.Decision.APPROVE) {

            membership.setStatus(MembershipStatus.ACTIVE);
            membership.setClubRole(ClubRole.MEMBER);
            membership.setJoinedAt(Instant.now());

        } else {

            membership.setStatus(MembershipStatus.REJECTED);
            membership.setClubRole(ClubRole.MEMBER);
            membership.setJoinedAt(null);
        }

        return toResponse(clubMembershipRepo.save(membership));    
            }

     @Transactional 
     public  void  leaveClub(UUID clubId, String email){
         User user = findUser(email);
        Club club = findClub(clubId);

        ClubMembership membership =clubMembershipRepo.findByUser_IdAndClub_Id(
                                user.getId(),
                                club.getId()
                        )
                        .orElseThrow(() -> new ApiException(
                                HttpStatus.NOT_FOUND,
                                "MEMBERSHIP_NOT_FOUND",
                                "Your membership record was not found."
                        ));

        if (membership.getStatus() != MembershipStatus.ACTIVE) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "NOT_AN_ACTIVE_MEMBER",
                    "You do not have an active membership in this club."
            );
        }

        if (membership.getClubRole() == ClubRole.PRESIDENT) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "PRESIDENT_TRANSFER_REQUIRED",
                    "Transfer club presidency before leaving the club."
            );
        }

        if (membership.getClubRole() == ClubRole.FACULTY_ADVISOR) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "ADVISOR_ASSIGNMENT_REQUIRED",
                    "Remove the faculty advisor assignment before leaving."
            );
        }

        membership.setStatus(MembershipStatus.LEFT);
        clubMembershipRepo.save(membership);
     }
    

     private User findUser(String email) {

        String normalizedEmail = email
                .trim()
                .toLowerCase(Locale.ROOT);

        return userRepo.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "USER_NOT_FOUND",
                        "The user account was not found."
                ));
    }

    private Club findClub(UUID clubId) {

        return clubRepo.findById(clubId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "CLUB_NOT_FOUND",
                        "The club was not found."
                ));
    }

    private Club findActiveClub(UUID clubId) {
        Club club = findClub(clubId);

        if (club.getStatus() != ClubStatus.ACTIVE) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "CLUB_NOT_ACTIVE",
                    "Membership requests are not accepted for this club."
            );
        }

        return club;
    }

    private void requireClubReviewer(User actor, Club club) {

        if (actor.getSystemRole() == SystemRole.ADMIN) {
            return;
        }
        if (club.getFacultyAdvisor() != null
                && club.getFacultyAdvisor().getId()
                        .equals(actor.getId())) {
            return;
        }

        boolean hasReviewerRole =
                clubMembershipRepo.existsByClub_IdAndUser_IdAndStatusAndClubRoleIn(
                                club.getId(),
                                actor.getId(),
                                MembershipStatus.ACTIVE,
                                List.of(
                                        ClubRole.PRESIDENT,
                                        ClubRole.SECRETARY,
                                        ClubRole.FACULTY_ADVISOR
                                )
                        );

        if (!hasReviewerRole) {
            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "CLUB_ACCESS_DENIED",
                    "You do not have review permission for this club."
            );
        }
    }

    private void validatePagination(int page, int size) {

        if (page < 0 || size < 1 || size > 50) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_PAGINATION",
                    "Page must be non-negative and size must be between 1 and 50."
            );
        }
    }

    private String normalizeOptional(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private MembershipResponse toResponse(  ClubMembership membership
    ) {

        return new MembershipResponse(
                membership.getId(),
                membership.getClub().getId(),
                membership.getClub().getName(),
                membership.getUser().getId(),
                membership.getUser().getFullName(),
                membership.getClubRole(),
                membership.getStatus(),
                membership.getRequestMessage(),
                membership.getReviewReason(),
                membership.getRequestedAt(),
                membership.getReviewedBy() == null
                        ? null
                        : membership.getReviewedBy().getId(),
                membership.getReviewedAt(),
                membership.getJoinedAt()
        );
    }

}
