package com.rental_api.ServiceBooking.Services.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rental_api.ServiceBooking.Dto.GoogleUserInfo;
import com.rental_api.ServiceBooking.Dto.Response.AuthResponse;
import com.rental_api.ServiceBooking.Entity.Role;
import com.rental_api.ServiceBooking.Entity.User;
import com.rental_api.ServiceBooking.Repository.RoleRepository;
import com.rental_api.ServiceBooking.Repository.UserRepository;
import com.rental_api.ServiceBooking.Security.JwtUtils;
import com.rental_api.ServiceBooking.Services.GoogleOAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
public class GoogleOAuthServiceImpl implements GoogleOAuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    private final RestTemplate restTemplate = new RestTemplate();

    // Google config
    @Value("${google.client-id}")
    private String googleClientId;

    @Value("${google.client-secret}")
    private String googleClientSecret;

    @Value("${google.redirect-uri}")
    private String googleRedirectUri;

    // ================= GOOGLE LOGIN =================
    @Override
    public AuthResponse loginWithGoogle(String code) {
        try {

            // Exchange code for access token
            String tokenUrl = "https://oauth2.googleapis.com/token";

            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("code", code);
            map.add("client_id", googleClientId);
            map.add("client_secret", googleClientSecret);
            map.add("redirect_uri", googleRedirectUri);
            map.add("grant_type", "authorization_code");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<MultiValueMap<String, String>> request =
                    new HttpEntity<>(map, headers);

            ResponseEntity<String> tokenResponse =
                    restTemplate.postForEntity(tokenUrl, request, String.class);

            JsonNode tokenJson = objectMapper.readTree(tokenResponse.getBody());
            String accessToken = tokenJson.get("access_token").asText();

            // Get user info
            HttpHeaders authHeaders = new HttpHeaders();
            authHeaders.setBearerAuth(accessToken);

            HttpEntity<String> userRequest = new HttpEntity<>(authHeaders);

            ResponseEntity<String> userResponse = restTemplate.exchange(
                    "https://www.googleapis.com/oauth2/v2/userinfo",
                    HttpMethod.GET,
                    userRequest,
                    String.class
            );

            GoogleUserInfo googleUser =
                    objectMapper.readValue(userResponse.getBody(), GoogleUserInfo.class);

            return processOAuthUser(
                    googleUser.getEmail(),
                    googleUser.getName(),
                    googleUser.getPicture()
            );

        } catch (Exception e) {
            throw new RuntimeException("Google Login Error: " + e.getMessage(), e);
        }
    }

    // ================= COMMON USER PROCESS =================
    private AuthResponse processOAuthUser(String email, String name, String avatar) {

        User user = userRepository.findByEmail(email).orElseGet(() -> {

            Role role = roleRepository.findByName("student")
                    .orElseThrow(() -> new RuntimeException("Role not found"));

            return userRepository.save(
                    User.builder()
                            .fullname(name)
                            .email(email)
                            .avatarUrl(avatar)
                            .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                            .status(User.Status.ACTIVE)
                            .roles(Set.of(role))
                            .build()
            );
        });

        // Update user data
        if (avatar != null) {
            user.setAvatarUrl(avatar);
        }

        userRepository.save(user);

        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .toList();

        // Generate JWT
        String token = jwtUtils.generateToken(
                user.getId(),
                user.getEmail(),
                user.getFullname(),
                roles,
                List.of(1L)
        );

        return AuthResponse.builder()
                .userId(user.getId())
                .fullname(user.getFullname())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .token(token)
                .message("Login success via Google")
                .build();
    }
}