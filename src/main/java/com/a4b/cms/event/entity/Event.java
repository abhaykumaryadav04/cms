package com.a4b.cms.event.entity;



import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.Check;

import com.a4b.cms.club.entity.Club;
import com.a4b.cms.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor 
@AllArgsConstructor 
@Builder 
@Table(
    name = "events",
    indexes = {
        @Index(
            name = "idx_events_club_status",
            columnList = "club_id, status"
        ),
        @Index(
            name = "idx_events_status_start",
            columnList = "status, starts_at"
        ),
        @Index(
            name = "idx_events_registration_deadline",
            columnList = "registration_deadline"
        )
    }
)
@Check(
    constraints = "capacity IS NULL OR capacity > 0"
)
@Data 
public class Event {
  
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "club_id", nullable = false)
    private Club club;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "created_by",
        nullable = false,
        updatable = false
    )
    private User createdBy;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 250)
    private String venue;

    @Column(
        name = "starts_at",
        nullable = false,
        columnDefinition = "TIMESTAMP WITH TIME ZONE"
    )
    private Instant startsAt;

    @Column(
        name = "ends_at",
        nullable = false,
        columnDefinition = "TIMESTAMP WITH TIME ZONE"
    )
    private Instant endsAt;

    @Column
    private Integer capacity;

    @Column(
        name = "registration_deadline",
        nullable = false,
        columnDefinition = "TIMESTAMP WITH TIME ZONE"
    )
    private Instant registrationDeadline;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EventStatus status ;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Column(
        name = "approved_at",
        columnDefinition = "TIMESTAMP WITH TIME ZONE"
    )
    private Instant approvedAt;

    @Column(
        name = "cancellation_reason",
        columnDefinition = "TEXT"
    )
    private String cancellationReason;
    @Column(
        name = "created_at",
        nullable = false,
        updatable = false,
        columnDefinition = "TIMESTAMP WITH TIME ZONE"
    )
    private Instant createdAt;
    @Column(
        name = "updated_at",
        nullable = false,
        columnDefinition = "TIMESTAMP WITH TIME ZONE"
    )
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {

        Instant now = Instant.now();

        createdAt = now;
        updatedAt = now;

        if (status == null) {
            status = EventStatus.DRAFT;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
