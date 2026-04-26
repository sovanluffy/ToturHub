package com.rental_api.ServiceBooking.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.rental_api.ServiceBooking.Dto.Request.ReviewRequest;
import com.rental_api.ServiceBooking.Dto.Response.GetReviewResponse;
import com.rental_api.ServiceBooking.Dto.Response.ReviewResponse;
import com.rental_api.ServiceBooking.Services.ReviewService;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // =====================================================
    // ================= CREATE REVIEWS ====================
    // =====================================================

    /**
     * POST /api/v1/reviews/class/{classId}
     * Students use this to review a specific class they have attended (CONFIRMED booking required).
     */
    @PostMapping("/class/{classId}")
    public ResponseEntity<ReviewResponse> createClassReview(
            @PathVariable Long classId,
            @RequestBody ReviewRequest reviewRequest) {

        return ResponseEntity.ok(reviewService.createClassReview(classId, reviewRequest));
    }

    /**
     * POST /api/v1/reviews/tutor/{tutorId}
     * Students can review a tutor directly (across all their classes).
     */
    @PostMapping("/tutor/{tutorId}")
    public ResponseEntity<ReviewResponse> createTutorReview(
            @PathVariable Long tutorId,
            @RequestBody ReviewRequest reviewRequest) {

        return ResponseEntity.ok(reviewService.createTutorReview(tutorId, reviewRequest));
    }

    // =====================================================
    // ================= GET REVIEWS =======================
    // =====================================================

    /**
     * GET /api/v1/reviews/class/{classId}
     * Fetch all reviews, average rating, and total count for a specific class.
     */
    @GetMapping("/class/{classId}")
    public ResponseEntity<GetReviewResponse> getReviewsByClassId(@PathVariable Long classId) {
        return ResponseEntity.ok(reviewService.getReviewsByClassId(classId));
    }

    /**
     * GET /api/v1/reviews/tutor/{tutorId}
     * Fetch overall reviews and average rating for a tutor across all their classes.
     */
    @GetMapping("/tutor/{tutorId}")
    public ResponseEntity<GetReviewResponse> getReviewsByTutorId(@PathVariable Long tutorId) {
        return ResponseEntity.ok(reviewService.getReviewByTutorId(tutorId));
    }
}