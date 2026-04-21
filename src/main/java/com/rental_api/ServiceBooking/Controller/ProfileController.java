package com.rental_api.ServiceBooking.Controller;

import com.rental_api.ServiceBooking.Dto.Response.ProfileResponse;
import com.rental_api.ServiceBooking.Dto.Response.AuthResponse;
import com.rental_api.ServiceBooking.Services.AuthService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/profile")
public class ProfileController {

    private final AuthService authService;

    // ================= GET PROFILE =================
    @GetMapping
    public ResponseEntity<ProfileResponse> getProfile() {
        try {
            ProfileResponse profile = authService.getProfileFromToken();
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            log.error("Error fetching profile", e);
            throw e; // let global handler handle it
        }
    }

    // ================= REQUEST TUTOR =================
    @PostMapping("/request-tutor")
    public ResponseEntity<AuthResponse> requestTutor() {
        try {
            AuthResponse response = authService.requestTutor();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error requesting tutor", e);
            throw e;
        }
    }
}