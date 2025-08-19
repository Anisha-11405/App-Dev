package com.examly.springapp.model;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.security.Key;

@Component
public class JwtUtil {

    private final String SECRET_KEY = "MySuperSecretKeyForJWTGeneration12345";
    private final long EXPIRATION_TIME = 1000 * 60 * 60; // 1 hour

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    // Generate token without role (basic use)
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Generate token with role (always store as ROLE_*)
    public String generateToken(String email, Role role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "ROLE_" + role.name());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String extractClaim(String token, String claimKey) {
        return (String) extractAllClaims(token).get(claimKey);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private boolean isTokenExpired(String token) {
        Date expiration = extractExpiration(token);
        boolean expired = expiration.before(new Date());
        System.out.println("⏰ JwtUtil - Token expiration check: " + expiration + ", Current time: " + new Date() + ", Expired: " + expired);
        return expired;
    }

    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);
            boolean valid = !isTokenExpired(token);
            System.out.println("🔍 JwtUtil - Token validation: " + (valid ? "Valid" : "Invalid"));
            return valid;
        } catch (JwtException | IllegalArgumentException e) {
            System.out.println("❌ JwtUtil - Token invalid: " + e.getMessage());
            return false;
        }
    }

    public boolean validateToken(String token, String username) {
        try {
            final String tokenUsername = extractUsername(token);
            boolean valid = tokenUsername.equals(username) && !isTokenExpired(token);
            System.out.println("🔐 JwtUtil - Token validation with username: " + username + ", Valid: " + valid);
            return valid;
        } catch (Exception e) {
            System.out.println("❌ JwtUtil - Validation error: " + e.getMessage());
            return false;
        }
    }
}
