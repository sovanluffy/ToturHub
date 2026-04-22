package com.rental_api.ServiceBooking.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rental_api.ServiceBooking.Dto.Request.ReviewRequest;
import com.rental_api.ServiceBooking.Dto.Response.GetReviewResponse;
import com.rental_api.ServiceBooking.Dto.Response.ReviewResponse;
import com.rental_api.ServiceBooking.Services.ReviewService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;




@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/{tutorId}")
    public ResponseEntity<ReviewResponse> createReview(
            @PathVariable Long tutorId,
            @RequestBody ReviewRequest reviewRequest   
    ) {
        return ResponseEntity.ok(
                reviewService.createReview(tutorId, reviewRequest)
        );
    }

    @GetMapping("/{tutorId}")
    public ResponseEntity<GetReviewResponse> getReviewsByTutorId(@PathVariable Long tutorId) {
        return ResponseEntity.ok(reviewService.getReviewByTutorId(tutorId));
    }
    
}
