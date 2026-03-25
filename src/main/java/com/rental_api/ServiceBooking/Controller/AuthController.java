package com.rental_api.ServiceBooking.Controller;

import com.rental_api.ServiceBooking.Dto.Request.LoginRequest;
import com.rental_api.ServiceBooking.Dto.Request.RegisterRequest;
import com.rental_api.ServiceBooking.Dto.Response.AuthResponse;
import com.rental_api.ServiceBooking.Services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // ------------------- REGISTER WITH AVATAR -------------------
    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AuthResponse> register(
            @RequestPart("user") @Valid String userJson,
            @RequestPart(value = "avatar", required = false) MultipartFile avatar
    ) throws Exception {

        // Deserialize JSON string to RegisterRequest
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        RegisterRequest registerRequest = mapper.readValue(userJson, RegisterRequest.class);

        AuthResponse response = authService.register(registerRequest, avatar);
        return ResponseEntity.ok(response);
    }

    // ------------------- LOGIN -------------------
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    // ------------------- REQUEST TUTOR -------------------
    @PostMapping("/request-tutor/{userId}")
    public ResponseEntity<AuthResponse> requestTutor(@PathVariable Long userId) {
        AuthResponse response = authService.requestTutor(userId);
        return ResponseEntity.ok(response);
    }

    // ------------------- ADMIN APPROVE -------------------
    @PostMapping("/approve-tutor/{userId}")
    public ResponseEntity<Void> approveTutor(@PathVariable Long userId) {
        authService.approveTutor(userId);
        return ResponseEntity.ok().build();
    }

    // ------------------- ADMIN REJECT -------------------
    @PostMapping("/reject-tutor/{userId}")
    public ResponseEntity<Void> rejectTutor(@PathVariable Long userId) {
        authService.rejectTutor(userId);
        return ResponseEntity.ok().build();
    }
}