package com.a4b.cms.auth.entity;

import java.time.Instant;
import java.util.UUID;


import jakarta.persistence.Id;
import com.a4b.cms.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Table(
    name = "refresh_tokens",
    indexes = {
        @Index(
            name = "idx_refresh_token_user",
            columnList = "user_id"
        ),
        @Index(
            name = "idx_refresh_token_expiry",
            columnList = "expires_at"
        )
    }
)
@Data 
@Builder 
@NoArgsConstructor 
@AllArgsConstructor 
@Entity 
public class RefreshToken {
   @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(
        name = "token_hash",
        nullable = false,
        unique = true,
        length = 64
    )
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;


    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

}
