package com.a4b.cms.membership.entity;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.Id;

import com.a4b.cms.club.entity.Club;
import com.a4b.cms.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "club_memberships",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_membership_user_club",
            columnNames = {"user_id", "club_id"}
        )
    },
    indexes = {
        @Index(
            name = "idx_membership_club_status",
            columnList = "club_id, status"
        ),
        @Index(
            name = "idx_membership_user_status",
            columnList = "user_id, status"
        )
    }
)
@Builder 
@Data 
@NoArgsConstructor 
@AllArgsConstructor 
public class ClubMembership {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "club_id", nullable = false)
    private Club club;

    @Enumerated(EnumType.STRING)
    @Column(name = "club_role", nullable = false, length = 30)
    private ClubRole clubRole ;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MembershipStatus status ;

    @Column(name = "request_message", columnDefinition = "TEXT")
    private String requestMessage;

    @Column(name = "review_reason", columnDefinition = "TEXT")
    private String reviewReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    @Column(name = "requested_at", nullable = false)
    private Instant requestedAt;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @Column(name = "joined_at")
    private Instant joinedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();

        createdAt = now;
        updatedAt = now;

        if (requestedAt == null) {
            requestedAt = now;
        }

        if (status == null) {
            status = MembershipStatus.PENDING;
        }

        if (clubRole == null) {
            clubRole = ClubRole.MEMBER;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}

