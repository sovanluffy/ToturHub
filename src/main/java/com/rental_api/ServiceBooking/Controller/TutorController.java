package com.rental_api.ServiceBooking.Controller;

import com.rental_api.ServiceBooking.Dto.Request.TutorProfileRequest;
import com.rental_api.ServiceBooking.Dto.Response.TutorFullViewResponse;
import com.rental_api.ServiceBooking.Services.TutorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/tutors")
@RequiredArgsConstructor
@Tag(name = "Tutor Management", description = "Endpoints for tutors to manage their profile and media assets")
public class TutorController {

    private final TutorService tutorService;

    // ---------------------------------------------------------
    // 1. UPDATE PROFILE & UPLOAD MEDIA
    // ---------------------------------------------------------
    @Operation(summary = "Update tutor profile (Bio, Education, Exp) and upload files to Cloudinary")
    @PutMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProfile(
            @RequestPart("data") TutorProfileRequest data,
            @RequestPart(value = "profileImg", required = false) MultipartFile profileImg,
            @RequestPart(value = "coverImg", required = false) MultipartFile coverImg,
            @RequestPart(value = "videoFile", required = false) MultipartFile videoFile,
            @RequestPart(value = "certificates", required = false) List<MultipartFile> certificates,
            @RequestParam(value = "publish", defaultValue = "false") boolean publish
    ) {
        try {
            // 1. Execute the update logic
            // Note: coverImg here matches the -F 'coverImg=...' in your curl
            tutorService.updateTutorProfile(data, profileImg, videoFile, coverImg, certificates);

            // 2. Handle the publish status toggle
            if (publish) {
                tutorService.publishProfile();
            } else {
                tutorService.unpublishProfile();
            }

            // 3. Re-fetch the profile 
            // This ensures the response contains the new Cloudinary URLs instead of null
            TutorFullViewResponse updatedProfile = tutorService.getMyOwnProfile();
            
            return ResponseEntity.ok(updatedProfile);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Server Error: " + e.getMessage()));
        }
    }

    // ---------------------------------------------------------
    // 2. PROFILE VISIBILITY (PUBLISH/UNPUBLISH)
    // ---------------------------------------------------------
    @Operation(summary = "Manually publish profile to make it visible to the public")
    @PostMapping("/publish")
    public ResponseEntity<?> publishProfile() {
        try {
            tutorService.publishProfile();
            return ResponseEntity.ok(tutorService.getMyOwnProfile());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Manually unpublish profile to hide it from search")
    @PostMapping("/unpublish")
    public ResponseEntity<?> unpublishProfile() {
        try {
            tutorService.unpublishProfile();
            return ResponseEntity.ok(tutorService.getMyOwnProfile());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // ---------------------------------------------------------
    // 3. PROFILE RETRIEVAL
    // ---------------------------------------------------------
    @Operation(summary = "Get the authenticated tutor's own profile")
    @GetMapping("/me")
    public ResponseEntity<?> getMyOwnProfile() {
        try {
            return ResponseEntity.ok(tutorService.getMyOwnProfile());
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Get full tutor details by ID (Public View)")
    @GetMapping("/{tutorId}")
    public ResponseEntity<?> getTutorFullDetail(@PathVariable Long tutorId) {
        try {
            return ResponseEntity.ok(tutorService.getTutorFullDetail(tutorId));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("error", "Tutor not found: " + e.getMessage()));
        }
    }

    // ---------------------------------------------------------
    // 4. ADMIN ACTIONS
    // ---------------------------------------------------------
    @Operation(summary = "Admin: Forcefully unpublish a tutor profile (e.g., for moderation)")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{tutorId}/admin-unpublish")
    public ResponseEntity<Map<String, String>> adminUnpublishTutor(@PathVariable Long tutorId) {
        try {
            tutorService.adminUnpublishTutor(tutorId);
            return ResponseEntity.ok(Map.of("message", "Tutor visibility disabled by administrator"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}