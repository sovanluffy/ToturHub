package com.rental_api.ServiceBooking.Controller;

import com.rental_api.ServiceBooking.Dto.Request.LoginRequest;
import com.rental_api.ServiceBooking.Dto.Request.RegisterRequest;
import com.rental_api.ServiceBooking.Dto.Response.AuthResponse;
import com.rental_api.ServiceBooking.Services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * REGISTER: Accepts form-data.
     * Use @RequestPart for the JSON body and the file separately.
     */
    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AuthResponse> register(
            @RequestPart("request") @Valid RegisterRequest request,
            @RequestPart(value = "avatar", required = false) MultipartFile avatar) {
        
        AuthResponse response = authService.register(request, avatar);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * LOGIN: Standard JSON request.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * REQUEST TUTOR ROLE: Changes status to PENDING and creates profile.
     */
    @PostMapping("/request-tutor/{userId}")
    public ResponseEntity<AuthResponse> requestTutor(@PathVariable Long userId) {
        return ResponseEntity.ok(authService.requestTutor(userId));
    }

    /**
     * APPROVE TUTOR: (Usually called by an Admin)
     */
    @PutMapping("/approve-tutor/{userId}")
    public ResponseEntity<Void> approveTutor(@PathVariable Long userId) {
        authService.approveTutor(userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * REJECT TUTOR: (Usually called by an Admin)
     */
    @PutMapping("/reject-tutor/{userId}")
    public ResponseEntity<Void> rejectTutor(@PathVariable Long userId) {
        authService.rejectTutor(userId);
        return ResponseEntity.noContent().build();
    }
}