package com.rental_api.ServiceBooking.Controller;

import com.rental_api.ServiceBooking.Dto.Request.LoginRequest;
import com.rental_api.ServiceBooking.Dto.Request.RegisterRequest;
import com.rental_api.ServiceBooking.Dto.Response.AuthResponse;
import com.rental_api.ServiceBooking.Entity.Role;
import com.rental_api.ServiceBooking.Services.AuthService;
import com.rental_api.ServiceBooking.Services.GoogleOAuthService;
import com.rental_api.ServiceBooking.Services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Authentication and User APIs")
@CrossOrigin(origins = "http://127.0.0.1:5500") 
public class AuthController {

    private final AuthService authService;
    private final GoogleOAuthService googleOAuthService;
    private final UserService userService;

    // 1. GET /auth/me - Get current user profile
    @GetMapping("/me")
    @Operation(summary = "Get current profile")
    public ResponseEntity<?> getMe() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return userService.getUserByEmail(auth.getName())
                .map(user -> ResponseEntity.ok(Map.of(
                        "userId", user.getId(),
                        "fullname", user.getFullname(),
                        "email", user.getEmail(),
                        "avatarUrl", user.getAvatarUrl() != null ? user.getAvatarUrl() : "",
                        "roles", user.getRoles().stream().map(Role::getName).collect(Collectors.toList()),
                        "status", user.getStatus()
                )))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // 2. GET /auth/google - Google OAuth Callback (THE ONLY ONE)
    @GetMapping("/google")
    @Operation(summary = "Google Login")
    public ResponseEntity<AuthResponse> loginWithGoogle(@RequestParam("code") String code) {
        return ResponseEntity.ok(googleOAuthService.loginWithGoogle(code));
    }

    // 3. POST /auth/register
    @PostMapping(value = "/register", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<AuthResponse> registerStudent(
            @RequestPart("data") RegisterRequest request,
            @RequestPart(value = "avatar", required = false) MultipartFile avatar) {
        return ResponseEntity.ok(authService.register(request, avatar));
    }

    // 4. POST /auth/login
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}