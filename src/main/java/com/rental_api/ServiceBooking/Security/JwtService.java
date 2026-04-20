package com.rental_api.ServiceBooking.Security;

import com.rental_api.ServiceBooking.Entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class JwtService {

    private final String SECRET = "secret-key";

    public String generateToken(User user) {

        Long tutorId = null;

        // ✅ SAFE ROLE CHECK (no getTutor() needed)
        if (user.getRoles() != null) {
            boolean isTutor = user.getRoles()
                    .stream()
                    .anyMatch(role -> role.getName().equals("TUTOR"));

            if (isTutor) {
                tutorId = user.getId(); // same userId used as tutorId
            }
        }

        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("userId", user.getId())
                .claim("email", user.getEmail())
                .claim("name", user.getFullname())
                .claim("tutorId", tutorId) // ✅ ADDED
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(SignatureAlgorithm.HS256, SECRET)
                .compact();
    }
}