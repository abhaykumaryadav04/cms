package com.a4b.cms.user.repo;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.a4b.cms.user.entity.User;

public interface UserRepo extends  JpaRepository<User,UUID>{
Optional<User> findByEmailIgnoreCase(String email); 
boolean existsByEmailIgnoreCase(String email);
boolean existsByEnrollNumberIgnoreCase(String enrollNumber); 
}
