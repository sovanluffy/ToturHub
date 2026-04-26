package com.rental_api.ServiceBooking.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.rental_api.ServiceBooking.Dto.Request.ReviewRequest;
import com.rental_api.ServiceBooking.Dto.Response.GetReviewResponse;
import com.rental_api.ServiceBooking.Dto.Response.ReviewResponse;
import com.rental_api.ServiceBooking.Services.ReviewService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // =====================================================
    // ================= CREATE REVIEW ======================
    // =====================================================

    /**
     * POST /api/v1/reviews/class/{classId}
     * Students use this to review a specific class they attended.
     */
    @PostMapping("/class/{classId}")
    public ResponseEntity<ReviewResponse> createClassReview(
            @PathVariable Long classId,
            @RequestBody ReviewRequest reviewRequest   
    ) {
        return ResponseEntity.ok(
                reviewService.createClassReview(classId, reviewRequest)
        );
    }

    // =====================================================
    // ================= GET REVIEWS =======================
    // =====================================================

    /**
     * GET /api/v1/reviews/class/{classId}
     * Fetch the "Total People" count, Average Rating, and all comments for a specific class.
     */
    @GetMapping("/class/{classId}")
    public ResponseEntity<GetReviewResponse> getReviewsByClassId(@PathVariable Long classId) {
        return ResponseEntity.ok(reviewService.getReviewsByClassId(classId));
    }

    /**
     * GET /api/v1/reviews/tutor/{tutorId}
     * Fetch the overall reputation of a tutor across all their classes.
     */
    @GetMapping("/tutor/{tutorId}")
    public ResponseEntity<GetReviewResponse> getReviewsByTutorId(@PathVariable Long tutorId) {
        return ResponseEntity.ok(reviewService.getReviewByTutorId(tutorId));
    }
}