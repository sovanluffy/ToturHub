package com.rental_api.ServiceBooking.Controller;

import com.rental_api.ServiceBooking.Dto.Response.ProfileResponse;
import com.rental_api.ServiceBooking.Dto.Response.AuthResponse;
import com.rental_api.ServiceBooking.Services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ProfileController {

    private final AuthService authService;

    @GetMapping("/api/v1/profile")
    public ResponseEntity<ProfileResponse> getProfile() {
        ProfileResponse profile = authService.getProfileFromToken();
        return ResponseEntity.ok(profile);
    }

    @PostMapping("/api/v1/profile/request-tutor")
    public ResponseEntity<AuthResponse> requestTutor() {
        AuthResponse response = authService.requestTutor();
        return ResponseEntity.ok(response);
    }
}