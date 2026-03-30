package com.rental_api.ServiceBooking.Controller;

import com.rental_api.ServiceBooking.Dto.Request.TutorProfileRequest;
import com.rental_api.ServiceBooking.Dto.Response.TutorFullViewResponse;
import com.rental_api.ServiceBooking.Services.TutorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tutors")
@RequiredArgsConstructor
public class TutorController {

    private final TutorService tutorService;

    /**
     * UPDATE PROFILE
     * Consumes multipart/form-data to handle:
     * 1. "data" -> The JSON metadata (Bio, Education, Experience)
     * 2. "profileImg" -> Single Image file
     * 3. "videoFile" -> Single Video file
     * 4. "certificates" -> List of Image files
     */
    @PutMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> updateProfile(
            @RequestPart("data") @Valid TutorProfileRequest data,
            @RequestPart(value = "profileImg", required = false) MultipartFile profileImg,
            @RequestPart(value = "videoFile", required = false) MultipartFile videoFile,
            @RequestPart(value = "certificates", required = false) List<MultipartFile> certificates) {

        tutorService.updateTutorProfile(data, profileImg, videoFile, certificates);
        return ResponseEntity.ok("Tutor profile updated successfully. Assets uploaded to Cloudinary.");
    }

    /**
     * GET LOGGED-IN TUTOR PROFILE
     */
    @GetMapping("/my-profile")
    public ResponseEntity<TutorFullViewResponse> getMyProfile() {
        return ResponseEntity.ok(tutorService.getMyOwnProfile());
    }

    /**
     * GET SPECIFIC TUTOR (Public View)
     */
    @GetMapping("/detail/{tutorId}")
    public ResponseEntity<TutorFullViewResponse> getTutorDetail(@PathVariable Long tutorId) {
        return ResponseEntity.ok(tutorService.getTutorFullDetail(tutorId));
    }

    /**
     * PUBLISH PROFILE (Set isPublic = true)
     */
    @PostMapping("/publish")
    public ResponseEntity<Void> publish() {
        tutorService.publishProfile();
        return ResponseEntity.ok().build();
    }

    /**
     * UNPUBLISH PROFILE (Set isPublic = false)
     */
    @PostMapping("/unpublish")
    public ResponseEntity<Void> unpublish() {
        tutorService.unpublishProfile();
        return ResponseEntity.ok().build();
    }
}