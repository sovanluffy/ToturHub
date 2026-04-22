package com.rental_api.ServiceBooking.Services;

import org.springframework.stereotype.Service;

import com.rental_api.ServiceBooking.Dto.Request.ReviewRequest;
import com.rental_api.ServiceBooking.Dto.Response.GetReviewResponse;
import com.rental_api.ServiceBooking.Dto.Response.ReviewResponse;


@Service
public interface ReviewService {

    ReviewResponse createReview(Long tutorId, ReviewRequest reviewRequest);    
    GetReviewResponse getReviewByTutorId(Long tutorId);

}
