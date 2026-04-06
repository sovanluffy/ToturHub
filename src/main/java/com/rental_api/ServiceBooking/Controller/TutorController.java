package com.rental_api.ServiceBooking.Controller;

import com.rental_api.ServiceBooking.Dto.Request.TutorProfileRequest;
import com.rental_api.ServiceBooking.Dto.Response.TutorFullViewResponse;
import com.rental_api.ServiceBooking.Services.TutorService;
import io.swagger.v3.oas.annotations.Operation;
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
@Tag(name = "Tutor Management", description = "Endpoints for tutors to manage their profile and view tutor details")
public class TutorController {

    private final TutorService tutorService;

    // ------------------- UPDATE PROFILE -------------------
    @Operation(summary = "Update tutor profile and upload assets to Cloudinary")
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
            // 1. Call the service (matches your 'void' interface)
            tutorService.updateTutorProfile(data, profileImg, videoFile, coverImg, certificates);

            // 2. Handle the publishing toggle based on query parameter
            if (publish) {
                tutorService.publishProfile();
            } else {
                tutorService.unpublishProfile();
            }

            // 3. FETCH the updated profile to return as the response body
            // This ensures the frontend gets the NEW Cloudinary URLs immediately
            TutorFullViewResponse updatedProfile = tutorService.getMyOwnProfile();
            
            return ResponseEntity.ok(updatedProfile);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal Server Error: " + e.getMessage()));
        }
    }

    // ------------------- PUBLISH PROFILE -------------------
    @Operation(summary = "Publish profile to make it visible to students")
    @PostMapping("/publish")
    public ResponseEntity<?> publishProfile() {
        try {
            tutorService.publishProfile();
            return ResponseEntity.ok(tutorService.getMyOwnProfile());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // ------------------- UNPUBLISH PROFILE -------------------
    @Operation(summary = "Unpublish profile to make it invisible to students")
    @PostMapping("/unpublish")
    public ResponseEntity<?> unpublishProfile() {
        try {
            tutorService.unpublishProfile();
            return ResponseEntity.ok(tutorService.getMyOwnProfile());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // -------------------- ADMIN UNPUBLISH TUTOR --------------------
    @Operation(summary = "Admin: Unpublish a tutor profile")
    @PreAuthorize("hasRole('admin')")
    @PutMapping("/{tutorId}/admin-unpublish")
    public ResponseEntity<Map<String, String>> adminUnpublishTutor(@PathVariable Long tutorId) {
        tutorService.adminUnpublishTutor(tutorId);
        return ResponseEntity.ok(Map.of("message", "Tutor unpublished successfully by admin"));
    }

    // ------------------- GET MY OWN PROFILE -------------------
    @Operation(summary = "Get my own tutor profile")
    @GetMapping("/me")
    public ResponseEntity<?> getMyOwnProfile() {
        try {
            return ResponseEntity.ok(tutorService.getMyOwnProfile());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // ------------------- GET TUTOR FULL DETAIL BY ID -------------------
    @Operation(summary = "Get full tutor profile by ID")
    @GetMapping("/{tutorId}")
    public ResponseEntity<?> getTutorFullDetail(@PathVariable Long tutorId) {
        try {
            return ResponseEntity.ok(tutorService.getTutorFullDetail(tutorId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}