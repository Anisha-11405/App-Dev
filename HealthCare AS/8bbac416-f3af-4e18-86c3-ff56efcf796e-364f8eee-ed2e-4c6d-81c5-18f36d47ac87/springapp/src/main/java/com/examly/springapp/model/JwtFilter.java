package com.examly.springapp.model;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.Collections;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws java.io.IOException, jakarta.servlet.ServletException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        System.out.println("JwtFilter - Processing request: " + method + " " + path);

        if (path.startsWith("/auth/")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        String jwt = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);
            try {
                if (jwtUtil.isTokenValid(jwt)) {
                    String username = jwtUtil.extractUsername(jwt);
                    String roleClaim = jwtUtil.extractClaim(jwt, "role");

                    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(
                                        username,
                                        null,
                                        Collections.singletonList(new SimpleGrantedAuthority(roleClaim))
                                );
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        System.out.println("JwtFilter - Authentication set for user: " + username + " with role: " + roleClaim);
                    }
                } else {
                    System.out.println("JwtFilter - Token is invalid or expired");
                }

            } catch (Exception e) {
                System.out.println("JwtFilter - Invalid JWT token: " + e.getMessage());
            }
        } else {
            System.out.println("JwtFilter - No Bearer token found");
        }

        filterChain.doFilter(request, response);
    }
}