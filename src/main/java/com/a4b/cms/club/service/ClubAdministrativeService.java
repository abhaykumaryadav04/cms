package com.a4b.cms.club.service;

import java.util.Locale;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.a4b.cms.club.dto.AssignFacultyAdvisorRequest;
import com.a4b.cms.club.dto.AssignclubRoleRequest;
import com.a4b.cms.club.dto.ClubLeadershipResponse;
import com.a4b.cms.club.dto.ClubResponse;
import com.a4b.cms.club.entity.Club;
import com.a4b.cms.club.entity.ClubStatus;
import com.a4b.cms.club.repo.ClubRepo;
import com.a4b.cms.common.exception.ApiException;
import com.a4b.cms.membership.entity.ClubMembership;
import com.a4b.cms.membership.entity.ClubRole;
import com.a4b.cms.membership.entity.MembershipStatus;
import com.a4b.cms.membership.repo.ClubMembershipRepo;
import com.a4b.cms.user.entity.AccountStatus;
import com.a4b.cms.user.entity.SystemRole;
import com.a4b.cms.user.entity.User;
import com.a4b.cms.user.repo.UserRepo;

@Service 
public class ClubAdministrativeService {
    private final ClubRepo clubRepo;
    private final UserRepo userRepo;
    private final ClubMembershipRepo clubMembershipRepo;
    public ClubAdministrativeService(UserRepo userRepo,ClubMembershipRepo clubMembershipRepo,ClubRepo clubRepo){
     this.clubMembershipRepo=clubMembershipRepo;
     this.clubRepo=clubRepo;
     this.userRepo=userRepo;
    }
    @Transactional 
    public ClubLeadershipResponse assignClubRole( UUID clubId,
            UUID userId,String adminEmail, AssignclubRoleRequest request){
         requireActiveAdmin(adminEmail);
         Club club=findActiveClubForUpdate(clubId);
         User targetUser=userRepo.findById(userId).orElseThrow(()->
         new ApiException(HttpStatus.NOT_FOUND,"USER_NOT_FOUND","this user cannot found"));
          if (targetUser.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "TARGET_ACCOUNT_DISABLED",
                    "A disabled account cannot be assigned a club role.");
        }
        ClubMembership clubMembership=clubMembershipRepo.findByUser_IdAndClub_Id(userId, clubId).orElseThrow(()->
         new ApiException(HttpStatus.NOT_FOUND,"MEMBERSHIP_NOT_FOUND","role doesnot  found"));
           if (clubMembership.getStatus()!= MembershipStatus.ACTIVE) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "ACTIVE_MEMBERSHIP_REQUIRED",
                    "Only an active club member can be assigned a leadership role.");
        }
          ClubRole requestedRole = ClubRole.valueOf(
                request.role().name()
        );

        if (requestedRole == ClubRole.PRESIDENT) {

            var existingPresident = clubMembershipRepo.findFirstByClub_IdAndStatusAndClubRole(
                                    clubId,
                                    MembershipStatus.ACTIVE,
                                    ClubRole.PRESIDENT
                            );

    if (existingPresident.isPresent()&& !existingPresident.get().getUser().getId().equals(userId)) {

                throw new ApiException(
                        HttpStatus.CONFLICT,
                        "PRESIDENT_ALREADY_ASSIGNED",
                        "This club already has an active president. Reassign the existing presidency first."
                );
            }
        }

        clubMembership.setClubRole(requestedRole);

        ClubMembership savedMembership =
               clubMembershipRepo.save(clubMembership);

        return new ClubLeadershipResponse(
                club.getId(),
                club.getName(),
                savedMembership.getUser().getId(),
                savedMembership.getUser().getFullName(),
                savedMembership.getClubRole(),
                savedMembership.getStatus(),
                savedMembership.getUpdatedAt()
        );
            }
     @Transactional
    public ClubResponse assignFacultyAdvisor(
            UUID clubId,
            String adminEmail,
            AssignFacultyAdvisorRequest request) {

        requireActiveAdmin(adminEmail);
        Club club = findActiveClubForUpdate(clubId);
        if (request.userId() == null) {
            club.setFacultyAdvisor(null);

        } else {

            User advisor = userRepo.findById(request.userId()).orElseThrow(() -> new ApiException(
                            HttpStatus.NOT_FOUND,
                            "USER_NOT_FOUND",
                            "The faculty advisor account was not found."));

            if (advisor.getAccountStatus()!= AccountStatus.ACTIVE) {

                throw new ApiException( HttpStatus.CONFLICT,
                        "ADVISOR_ACCOUNT_DISABLED",
                        "A disabled account cannot be assigned as faculty advisor." );
            }
            club.setFacultyAdvisor(advisor);
        }

        Club savedClub = clubRepo.save(club);
        return toClubResponse(savedClub);
    }
      private void requireActiveAdmin(String email) {

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        User admin = userRepo.findByEmailIgnoreCase(normalizedEmail).orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,"USER_NOT_FOUND",
                        "The requesting user was not found."));
        if (admin.getAccountStatus() != AccountStatus.ACTIVE|| admin.getSystemRole() != SystemRole.ADMIN) {

        throw new ApiException(
        HttpStatus.FORBIDDEN,"ADMIN_ACCESS_REQUIRED", "An active administrator account is required.");
        }
    }

    private Club findActiveClubForUpdate(UUID clubId) {

        Club club = clubRepo.findByIdForUpdate(clubId).orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "CLUB_NOT_FOUND",
                        "The requested club was not found."));
        if (club.getStatus() != ClubStatus.ACTIVE) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "CLUB_NOT_ACTIVE",
                    "Leadership assignments cannot be changed for an archived club."
            );
        }
        return club;
    }

    private ClubResponse toClubResponse(Club club) {
        return new ClubResponse(
                club.getId(),
                club.getName(),
                club.getSlug(),
                club.getDescription(),
                club.getCategory(),
                club.getLogoUrl(),
                club.getStatus(),
                club.getCreatedBy().getId(),
                club.getFacultyAdvisor() == null
                        ? null
                        : club.getFacultyAdvisor().getId(),
                club.getCreatedAt(),
                club.getUpdatedAt()
        );
    }

}
