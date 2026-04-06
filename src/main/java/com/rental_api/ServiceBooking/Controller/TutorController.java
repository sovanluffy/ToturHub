package com.rental_api.ServiceBooking.Controller;

import com.rental_api.ServiceBooking.Dto.Request.TutorProfileRequest;
import com.rental_api.ServiceBooking.Dto.Response.TutorFullViewResponse;
import com.rental_api.ServiceBooking.Services.TutorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/tutors")
@RequiredArgsConstructor
@Tag(name = "Tutor Management", description = "Endpoints for tutors to manage their profile and visibility")
public class TutorController {

private final TutorService tutorService;
    // ---------------------------------------------------------
    // UPDATE PROFILE (Multipart Data)
    // ---------------------------------------------------------
    @Operation(summary = "Update tutor profile and upload media")
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
            // ✅ MATCHES SERVICE: (request, profileImg, videoFile, coverImage, certificates)
            tutorService.updateTutorProfile(data, profileImg, videoFile, coverImg, certificates);

            // Handle visibility toggle
            if (publish) {
                tutorService.publishProfile();
            } else {
                tutorService.unpublishProfile();
            }

            return ResponseEntity.ok(Map.of(
                "message", "Tutor profile updated successfully!",
                "status", 200
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating tutor profile: ", e);
            return ResponseEntity.status(500).body(Map.of("error", "Internal Server Error: " + e.getMessage()));
        }
    }

    // ---------------------------------------------------------
    // VISIBILITY CONTROLS (Publish / Unpublish)
    // ---------------------------------------------------------
    @Operation(summary = "Publish profile to make it visible to students")
    @PostMapping("/publish")
    public ResponseEntity<?> publishProfile() {
        try {
            tutorService.publishProfile();
            return ResponseEntity.ok(Map.of("message", "Profile is now public!", "status", 200));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Unpublish profile to hide it from students")
    @PostMapping("/unpublish")
    public ResponseEntity<?> unpublishProfile() {
        try {
            tutorService.unpublishProfile();
            return ResponseEntity.ok(Map.of("message", "Profile is now hidden", "status", 200));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // ---------------------------------------------------------
    // PROFILE DATA FETCHING
    // ---------------------------------------------------------
    @Operation(summary = "Get current logged-in tutor profile")
    @GetMapping("/me")
    public ResponseEntity<TutorFullViewResponse> getMyOwnProfile() {
        return ResponseEntity.ok(tutorService.getMyOwnProfile());
    }

    @Operation(summary = "Get full tutor profile by ID")
    @GetMapping("/{tutorId}")
    public ResponseEntity<TutorFullViewResponse> getTutorFullDetail(@PathVariable Long tutorId) {
        return ResponseEntity.ok(tutorService.getTutorFullDetail(tutorId));
    }

    // ---------------------------------------------------------
    // ADMIN ACTIONS
    // ---------------------------------------------------------
    @Operation(summary = "Admin only: Unpublish a tutor profile")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{tutorId}/admin-unpublish")
    public ResponseEntity<?> adminUnpublishTutor(@PathVariable Long tutorId) {
        try {
            tutorService.adminUnpublishTutor(tutorId);
            return ResponseEntity.ok(Map.of("message", "Tutor profile hidden by administrator"));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("error", "Tutor not found"));
        }
    }
}