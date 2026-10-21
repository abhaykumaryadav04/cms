package com.a4b.cms.common.config;

import java.io.IOException;
import java.util.UUID;

import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class RequestIdFilter extends OncePerRequestFilter {
    public static final String REQUEST_ID_ATTRIBUTE =
            RequestIdFilter.class.getName() + ".REQUEST_ID";
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
       String requestId = UUID.randomUUID().toString();

        request.setAttribute(
                REQUEST_ID_ATTRIBUTE,
                requestId
        );

        response.setHeader(
                "X-Request-Id",
                requestId
        );

        filterChain.doFilter(request, response);
    }

}
