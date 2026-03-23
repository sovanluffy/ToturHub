package com.rental_api.ServiceBooking.Services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rental_api.ServiceBooking.Dto.GoogleUserInfo;
import com.rental_api.ServiceBooking.Dto.Response.AuthResponse;
import com.rental_api.ServiceBooking.Entity.Role;
import com.rental_api.ServiceBooking.Entity.User;
import com.rental_api.ServiceBooking.Exception.ResourceNotFoundException;
import com.rental_api.ServiceBooking.Repository.RoleRepository;
import com.rental_api.ServiceBooking.Repository.UserRepository;
import com.rental_api.ServiceBooking.Security.JwtUtils;
import com.rental_api.ServiceBooking.Services.GoogleOAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GoogleOAuthServiceImpl implements GoogleOAuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    @Value("${google.client-id}")
    private String clientId;

    @Value("${google.client-secret}")
    private String clientSecret;

    @Value("${google.redirect-uri}")
    private String redirectUri;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public AuthResponse loginWithGoogle(String code) {
        try {
            // -------------------- 1️⃣ Exchange code for access token --------------------
            String tokenUrl = "https://oauth2.googleapis.com/token";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("code", code);
            map.add("client_id", clientId);
            map.add("client_secret", clientSecret);
            map.add("redirect_uri", redirectUri);
            map.add("grant_type", "authorization_code");

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
            ResponseEntity<String> tokenResponse = restTemplate.postForEntity(tokenUrl, request, String.class);

            if (!tokenResponse.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Failed to get access token from Google");
            }

            // Parse access token from response
            String responseBody = tokenResponse.getBody();
            String accessToken = objectMapper.readTree(responseBody).get("access_token").asText();

            // -------------------- 2️⃣ Fetch user info --------------------
            HttpHeaders userHeaders = new HttpHeaders();
            userHeaders.setBearerAuth(accessToken);
            HttpEntity<String> userRequest = new HttpEntity<>(userHeaders);

            ResponseEntity<String> userResponse = restTemplate.exchange(
                    "https://www.googleapis.com/oauth2/v2/userinfo",
                    HttpMethod.GET,
                    userRequest,
                    String.class
            );

            GoogleUserInfo googleUser = objectMapper.readValue(userResponse.getBody(), GoogleUserInfo.class);

            // -------------------- 3️⃣ Find or create user --------------------
            User user = userRepository.findByEmail(googleUser.getEmail())
                    .orElseGet(() -> {
                        Role studentRole = roleRepository.findByName("student")
                                .orElseThrow(() -> new ResourceNotFoundException("Student role not found"));

                        // Generate a secure random password
                        String randomPassword = UUID.randomUUID().toString();

                        User newUser = User.builder()
                                .fullname(googleUser.getName())
                                .email(googleUser.getEmail())
                                .avatarUrl(googleUser.getPicture())
                                .password(passwordEncoder.encode(randomPassword)) // secure random password
                                .status(User.Status.ACTIVE)
                                .roles(Set.of(studentRole))
                                .build();

                        return userRepository.save(newUser);
                    });

            // -------------------- 4️⃣ Build JWT --------------------
            List<String> roleNames = user.getRoles().stream().map(Role::getName).collect(Collectors.toList());
            List<Long> roleIds = user.getRoles().stream().map(Role::getId).collect(Collectors.toList());
            String token = jwtUtils.generateToken(user.getId(), user.getEmail(), user.getEmail(), roleNames, roleIds);

            // -------------------- 5️⃣ Return AuthResponse --------------------
            return AuthResponse.builder()
                    .userId(user.getId())
                    .fullname(user.getFullname())
                    .email(user.getEmail())
                    .avatarUrl(user.getAvatarUrl())
                    .roles(roleNames)
                    .roleIds(roleIds)
                    .token(token)
                    .message("Login with Google successful")
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Google login failed: " + e.getMessage(), e);
        }
    }
}