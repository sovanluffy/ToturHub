package com.rental_api.ServiceBooking.Services;

import com.rental_api.ServiceBooking.Dto.Request.ReviewRequest;
import com.rental_api.ServiceBooking.Dto.Response.GetReviewResponse;
import com.rental_api.ServiceBooking.Dto.Response.ReviewResponse;

public interface ReviewService {

    /**
     * Legacy method - kept for backward compatibility
     * (You can discourage its use in the implementation)
     */
    ReviewResponse createReview(Long tutorId, ReviewRequest reviewRequest);

    /**
     * Create a review for a specific class
     * Students can only review classes they have CONFIRMED booking for
     */
    ReviewResponse createClassReview(Long classId, ReviewRequest reviewRequest);

    /**
     * Create a review directly for a tutor (across all their classes)
     */
    ReviewResponse createTutorReview(Long tutorId, ReviewRequest reviewRequest);

    /**
     * Get all reviews for a specific class + average rating
     */
    GetReviewResponse getReviewsByClassId(Long classId);

    /**
     * Get all reviews for a tutor + overall average rating
     */
    GetReviewResponse getReviewByTutorId(Long tutorId);
}