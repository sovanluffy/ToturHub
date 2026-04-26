package com.rental_api.ServiceBooking.Services;

import com.rental_api.ServiceBooking.Dto.Request.ReviewRequest;
import com.rental_api.ServiceBooking.Dto.Response.GetReviewResponse;
import com.rental_api.ServiceBooking.Dto.Response.ReviewResponse;

public interface ReviewService {
    // This must exist for the Implementation to use @Override
    ReviewResponse createReview(Long tutorId, ReviewRequest reviewRequest);    
    
    ReviewResponse createClassReview(Long classId, ReviewRequest reviewRequest);    
    
    GetReviewResponse getReviewsByClassId(Long classId);
    
    GetReviewResponse getReviewByTutorId(Long tutorId);
}