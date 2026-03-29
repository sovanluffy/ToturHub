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
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Prevents CORS issues with your frontend
public class AuthController {

    private final AuthService authService;

    /**
     * REGISTER WITH CLOUDINARY AVATAR
     * Postman 'body' setup: 
     * 1. Select 'form-data'
     * 2. Key: "user" (Type: Text)   -> Value: { "fullname": "Alice", "email": "alice@cl.com", ... }
     * 3. Key: "avatar" (Type: File) -> Value: [Select your image file]
     */
    @PostMapping(value = "/register-with-avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AuthResponse> registerWithAvatar(
            @RequestPart("user") @Valid RegisterRequest registerRequest,
            @RequestPart(value = "avatar", required = false) MultipartFile avatar
    ) {
        // This method calls the service, which calls Cloudinary and saves the HTTPS link
        AuthResponse response = authService.register(registerRequest, avatar);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     *  LOGIN
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 🎓 REQUEST TUTOR ROLE
     * Adds 'Tutor' role to user and creates a blank Tutor profile.
     */
    @PostMapping("/request-tutor/{userId}")
    public ResponseEntity<AuthResponse> requestTutor(@PathVariable Long userId) {
        AuthResponse response = authService.requestTutor(userId);
        return ResponseEntity.ok(response);
    }

    /**
     *  ADMIN APPROVE TUTOR
     * Activates the tutor profile and sets visibility to public.
     */
    @PostMapping("/approve-tutor/{userId}")
    public ResponseEntity<String> approveTutor(@PathVariable Long userId) {
        authService.approveTutor(userId);
        return ResponseEntity.ok("Tutor approved successfully. Profile is now live.");
    }

    /**
     *  ADMIN REJECT TUTOR
     */
    @PostMapping("/reject-tutor/{userId}")
    public ResponseEntity<String> rejectTutor(@PathVariable Long userId) {
        authService.rejectTutor(userId);
        return ResponseEntity.ok("Tutor request has been rejected.");
    }
}