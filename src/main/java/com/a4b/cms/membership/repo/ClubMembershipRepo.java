package com.a4b.cms.membership.repo;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.a4b.cms.membership.entity.ClubMembership;
import com.a4b.cms.membership.entity.ClubRole;
import com.a4b.cms.membership.entity.MembershipStatus;

public interface ClubMembershipRepo extends JpaRepository<ClubMembership,UUID>{
      Optional<ClubMembership> findByUser_IdAndClub_Id(
            UUID userId,
            UUID clubId
    );

    Page<ClubMembership> findByUser_Id(
            UUID userId,
            Pageable pageable
    );

    Page<ClubMembership> findByClub_IdAndStatus(
            UUID clubId,
            MembershipStatus status,
            Pageable pageable
    );

    boolean existsByClub_IdAndUser_IdAndStatusAndClubRoleIn(
            UUID clubId,
            UUID userId,
            MembershipStatus status,
            Collection<ClubRole> clubRoles
    );
    Optional<ClubMembership> findFirstByClub_IdAndStatusAndClubRole(
        UUID clubId,
        MembershipStatus status,
        ClubRole clubRole
);

}
