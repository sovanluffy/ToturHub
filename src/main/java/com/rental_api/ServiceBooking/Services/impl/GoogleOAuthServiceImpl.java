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

    // Facebook config
    @Value("${facebook.client-id}")
    private String facebookClientId;

    @Value("${facebook.client-secret}")
    private String facebookClientSecret;

    @Value("${facebook.redirect-uri}")
    private String facebookRedirectUri;

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
                    googleUser.getPicture(),
                    "Google",
                    null
            );

        } catch (Exception e) {
            throw new RuntimeException("Google Login Error: " + e.getMessage());
        }
    }

    // ================= FACEBOOK LOGIN =================
    @Override
    public AuthResponse loginWithFacebook(String code) {
        try {

            // Exchange code for access token
            String tokenUrl =
                    "https://graph.facebook.com/v18.0/oauth/access_token" +
                            "?client_id=" + facebookClientId +
                            "&redirect_uri=" + facebookRedirectUri +
                            "&client_secret=" + facebookClientSecret +
                            "&code=" + code;

            ResponseEntity<String> tokenResponse =
                    restTemplate.getForEntity(tokenUrl, String.class);

            JsonNode tokenJson = objectMapper.readTree(tokenResponse.getBody());
            String accessToken = tokenJson.get("access_token").asText();

            // Get Facebook user info
            String userInfoUrl =
                    "https://graph.facebook.com/me?fields=id,name,email,picture.type(large)&access_token="
                            + accessToken;

            ResponseEntity<String> userResponse =
                    restTemplate.getForEntity(userInfoUrl, String.class);

            JsonNode root = objectMapper.readTree(userResponse.getBody());

            String id = root.get("id").asText();
            String name = root.get("name").asText();
            String picture = root.get("picture").get("data").get("url").asText();

            String email = root.has("email")
                    ? root.get("email").asText()
                    : id + "@facebook.com";

            return processOAuthUser(
                    email,
                    name,
                    picture,
                    "Facebook",
                    null
            );

        } catch (Exception e) {
            throw new RuntimeException("Facebook Login Error: " + e.getMessage());
        }
    }

    // ================= FACEBOOK DEAUTHORIZE =================
    @Override
    public void handleFacebookDeauthorize(String signedRequest) {
        try {

            String[] parts = signedRequest.split("\\.");
            if (parts.length < 2) {
                throw new RuntimeException("Invalid signed request");
            }

            String encodedPayload = parts[1];
            byte[] decodedBytes = Base64.getUrlDecoder().decode(encodedPayload);

            JsonNode data = objectMapper.readTree(decodedBytes);

            String fbUserId = data.get("user_id").asText();
            String email = fbUserId + "@facebook.com";

            userRepository.findByEmail(email).ifPresent(user -> {
                user.setStatus(User.Status.INACTIVE);
                userRepository.save(user);
            });

        } catch (Exception e) {
            throw new RuntimeException("Facebook Deauthorize Error: " + e.getMessage());
        }
    }

    // ================= COMMON USER PROCESS =================
    private AuthResponse processOAuthUser(
            String email,
            String name,
            String avatar,
            String provider,
            String phone
    ) {

        User user = userRepository.findByEmail(email).orElseGet(() -> {

            Role role = roleRepository.findByName("student")
                    .orElseThrow(() -> new RuntimeException("Role not found"));

            return userRepository.save(
                    User.builder()
                            .fullname(name)
                            .email(email)
                            .avatarUrl(avatar)
                            .phone(phone)
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

        if (phone != null) {
            user.setPhone(phone);
        }

        userRepository.save(user);

        // Generate roles list
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
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .token(token)
                .message("Login success via " + provider)
                .build();
    }
}