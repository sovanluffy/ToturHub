package com.rental_api.ServiceBooking.Security;

import com.rental_api.ServiceBooking.Entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class JwtUtils {

    public static final String CLAIM_USER_ID = "userId";
    public static final String CLAIM_EMAIL = "email";
    public static final String CLAIM_ROLES = "roles";
    public static final String CLAIM_ROLE_IDS = "roleIds";

    private final Key key;
    private final long expiration;

    public JwtUtils(
            @Value("${jwt.secret:}") String secret,
            @Value("${jwt.expiration:3600000}") long expiration
    ) {
        if (secret == null || secret.isBlank() || secret.length() < 64) {
            log.warn("JWT secret missing or too short! Using fallback secret for dev.");
            secret = "FallbackSecretKeyWith64PlusCharactersLongForDevOnly!1234567890ABCDEFGHIJ";
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.expiration = expiration;
    }

    public static User getCurrentUser() {
        return null;
    }

    public String generateToken(
            Long userId,
            String email,
            String username,
            List<String> roles,
            List<Long> roleIds
    ) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(username)
                .claim(CLAIM_USER_ID, userId)
                .claim(CLAIM_EMAIL, email)
                .claim(CLAIM_ROLES, roles)
                .claim(CLAIM_ROLE_IDS, roleIds)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expiration))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean validateToken(String token) {
        try {
            return getClaims(token).getExpiration().after(new Date());
        } catch (ExpiredJwtException e) {
            log.warn("JWT expired");
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT: {}", e.getMessage());
        }
        return false;
    }

    // ✅ extract userId
    public Long extractUserId(String token) {
        return getClaims(token).get(CLAIM_USER_ID, Long.class);
    }

    // ✅ extract email (FIX)
    public String extractEmail(String token) {
        return getClaims(token).get(CLAIM_EMAIL, String.class);
    }
}
