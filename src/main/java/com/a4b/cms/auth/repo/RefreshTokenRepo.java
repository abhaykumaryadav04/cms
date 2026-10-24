package com.a4b.cms.auth.repo;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;


import com.a4b.cms.auth.entity.RefreshToken;


public interface RefreshTokenRepo extends JpaRepository<RefreshToken,UUID> {
  Optional<RefreshToken> findByTokenHash(String tokenHash);
}
