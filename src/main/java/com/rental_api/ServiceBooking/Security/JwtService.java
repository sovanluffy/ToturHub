package com.rental_api.ServiceBooking.Security;

import com.rental_api.ServiceBooking.Entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {

    private static final String SECRET =
            "my-secret-key-my-secret-key-my-secret-key-123456";

    /* ================= SIGN KEY ================= */
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    /* ================= GENERATE TOKEN ================= */
    public String generateToken(User user) {

        Map<String, Object> claims = new HashMap<>();

        // ✅ BASIC INFO
        claims.put("userId", user.getId());
        claims.put("email", user.getEmail());
        claims.put("name", user.getFullname());

        // ✅ ROLES
        claims.put("roles",
                user.getRoles().stream()
                        .map(r -> r.getName())
                        .toList()
        );

        // ✅ TUTOR ID (SAFE)
        if (user.getTutor() != null) {
            claims.put("tutorId", user.getTutor().getId());
        }

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 1 day
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /* ================= EXTRACT ALL CLAIMS ================= */
    public Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /* ================= GET EMAIL ================= */
    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    /* ================= GET USER ID ================= */
    public Long extractUserId(String token) {
        return extractClaims(token).get("userId", Long.class);
    }

    /* ================= GET TUTOR ID ================= */
    public Long extractTutorId(String token) {
        Object tutorId = extractClaims(token).get("tutorId");
        return tutorId != null ? Long.valueOf(tutorId.toString()) : null;
    }

    /* ================= VALIDATE TOKEN ================= */
    public boolean isTokenValid(String token, String email) {
        return extractEmail(token).equals(email)
                && !isTokenExpired(token);
    }

    /* ================= CHECK EXPIRATION ================= */
    public boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }
}