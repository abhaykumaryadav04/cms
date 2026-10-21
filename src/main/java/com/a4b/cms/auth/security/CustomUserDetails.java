package com.a4b.cms.auth.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.a4b.cms.user.entity.AccountStatus;
import com.a4b.cms.user.entity.User;
import com.a4b.cms.user.repo.UserRepo;

public class CustomUserDetails implements  UserDetailsService{

private final UserRepo userRepo;

public CustomUserDetails(UserRepo userRepo){
    this.userRepo=userRepo;
}

@Override
public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
   User user = userRepo.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() ->new UsernameNotFoundException("User not found"));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .roles(user.getSystemRole().name())
                .disabled(
                        user.getAccountStatus()
                                != AccountStatus.ACTIVE
                )
                .build();
}

}
