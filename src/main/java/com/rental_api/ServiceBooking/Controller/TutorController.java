package com.rental_api.ServiceBooking.Controller;

import com.rental_api.ServiceBooking.Dto.Request.TutorProfileRequest;
import com.rental_api.ServiceBooking.Services.TutorService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/tutors")
@RequiredArgsConstructor
public class TutorController {

    private final TutorService tutorService;

    @Operation(summary = "Update tutor profile and upload assets to Cloudinary")
    @PutMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProfile(
            @RequestPart("data") TutorProfileRequest data,
            @RequestPart(value = "profileImg", required = false) MultipartFile profileImg,
            @RequestPart(value = "videoFile", required = false) MultipartFile videoFile,
            @RequestPart(value = "certificates", required = false) List<MultipartFile> certificates,
            @RequestParam(value = "publish", defaultValue = "false") boolean publish) {
        
        try {
            // 1. Save all data (Cloudinary + DB)
            tutorService.updateTutorProfile(data, profileImg, videoFile, certificates);
            
            // 2. Publish if requested
            if (publish) {
                tutorService.publishProfile();
            }
            
            return ResponseEntity.ok(Map.of("message", "Profile updated successfully!"));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal Server Error: " + e.getMessage()));
        }
    }

    @Operation(summary = "Publish profile to make it visible to students")
    @PostMapping("/publish")
    public ResponseEntity<?> publishOnly() {
        try {
            tutorService.publishProfile();
            return ResponseEntity.ok(Map.of("message", "Profile is now public!"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }
}