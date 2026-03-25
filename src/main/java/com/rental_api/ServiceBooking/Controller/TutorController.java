package com.rental_api.ServiceBooking.Controller;

import com.rental_api.ServiceBooking.Dto.Request.TutorProfileRequest;
import com.rental_api.ServiceBooking.Dto.Response.TutorFullViewResponse;
import com.rental_api.ServiceBooking.Services.TutorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tutors")
@RequiredArgsConstructor
public class TutorController {

    private final TutorService tutorService;

    /**
     * SAVE PROFESSIONAL PROFILE
     * Tutor clicks "Save" to update Bio, Video, Education, and Experience.
     */
    @PutMapping("/profile")
    public ResponseEntity<String> updateProfile(@RequestBody TutorProfileRequest request) {
        tutorService.updateTutorProfile(request);
        return ResponseEntity.ok("Professional profile and history updated successfully!");
    }

    /**
     * VIEW FULL TUTOR DETAILS (See All)
     * Student clicks on a Tutor to see their Video, Certificates, 
     * AND all the different classes they have posted.
     */
    @GetMapping("/{id}/full-details")
    public ResponseEntity<TutorFullViewResponse> getFullTutorDetail(@PathVariable Long id) {
        TutorFullViewResponse response = tutorService.getTutorFullDetail(id);
        return ResponseEntity.ok(response);
    }
}