package com.a4b.cms.common.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.a4b.cms.auth.security.JwtAuthenticationFilter;
import com.a4b.cms.auth.security.JwtService;
import com.a4b.cms.common.exception.SecurityExceptionHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain( HttpSecurity http,JwtService jwtService,
            UserDetailsService userDetailsService, SecurityExceptionHandler securityExceptionHandler
    ) throws Exception {

        JwtAuthenticationFilter jwtFilter =new JwtAuthenticationFilter( jwtService, userDetailsService);

        http.csrf(csrf -> csrf.disable())
              .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions ->
                        exceptions
                                .authenticationEntryPoint(securityExceptionHandler).accessDeniedHandler(securityExceptionHandler))
                .authorizeHttpRequests(auth -> auth

                         .requestMatchers(
                HttpMethod.POST,
                "/api/v1/auth/register",
                "/api/v1/auth/login"
                ).permitAll()
                 .requestMatchers( 
                HttpMethod.GET,
                "/api/v1/clubs",
                "/api/v1/clubs/*"
                ).permitAll()

              .requestMatchers(
                HttpMethod.POST,
                "/api/v1/clubs"
              ).hasRole("ADMIN")

               .requestMatchers("/error").permitAll()

                        .anyRequest().authenticated())
                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}