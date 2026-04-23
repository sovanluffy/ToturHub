package com.rental_api.ServiceBooking.Services.impl;

import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.rental_api.ServiceBooking.Dto.Request.ReviewRequest;
import com.rental_api.ServiceBooking.Dto.Response.GetReviewResponse;
import com.rental_api.ServiceBooking.Dto.Response.ReviewResponse;
import com.rental_api.ServiceBooking.Entity.Review;
import com.rental_api.ServiceBooking.Entity.Tutor;
import com.rental_api.ServiceBooking.Entity.User;
import com.rental_api.ServiceBooking.Entity.Enum.BookingStatus;
import com.rental_api.ServiceBooking.Exception.ResourceNotFoundException;
import com.rental_api.ServiceBooking.Repository.BookingRepository;
import com.rental_api.ServiceBooking.Repository.ReviewRepostory;
import com.rental_api.ServiceBooking.Repository.TutorRepository;
import com.rental_api.ServiceBooking.Repository.UserRepository;
import com.rental_api.ServiceBooking.Services.ReviewService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepostory reviewRepostory;
    private final TutorRepository tutorRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }


   @Override
    public ReviewResponse createReview(Long tutorId, ReviewRequest reviewRequest) {

    // 1. Current user
    User currentUser = getCurrentUser();

    // 2. Find tutor
    Tutor tutor = tutorRepository.findById(tutorId)
            .orElseThrow(() -> new ResourceNotFoundException("Tutor not found"));

    // 3. Validate booking
    boolean hasConfirmedBooking = bookingRepository
            .existsByUserAndOpenClass_TutorAndStatus(
                    currentUser,
                    tutor,
                    BookingStatus.CONFIRMED
            );

    if (!hasConfirmedBooking) {
        throw new IllegalStateException("You can only review tutors you have book confirmed");
    }

    // 4. Prevent duplicate review
    boolean alreadyReviewed = reviewRepostory
            .existsByStudentAndTutor(currentUser, tutor);

    if (alreadyReviewed) {
        throw new IllegalStateException("You already reviewed this tutor");
    }

    // 5. Validate rating manually (extra safety)
    if (reviewRequest.getRating() < 1 || reviewRequest.getRating() > 5) {
        throw new IllegalArgumentException("Rating must be between 1 and 5");
    }

    // 6. Save review
    Review review = Review.builder()
            .comment(reviewRequest.getComment())
            .rating(reviewRequest.getRating())
            .tutor(tutor)
            .student(currentUser)
            .build();

    Review saved = reviewRepostory.save(review);

    // 7. Return clean response
    return mapToResponse(saved);
    }

    @Override
    public GetReviewResponse getReviewByTutorId(Long tutorId) {
        
        Tutor tutor = tutorRepository.findById(tutorId)
                .orElseThrow(() -> new ResourceNotFoundException("Tutor not found"));
        
        List<Review> reviews = reviewRepostory.findAll()
                .stream()
                .filter(r -> r.getTutor().getId().equals(tutorId))
                .toList();
        
        double averageRating = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
        double roundedRating = Math.round(averageRating * 10.0) / 10.0;
        
        int totalReviewer = reviews.size();
        
        return GetReviewResponse.builder()
                .tutorId(tutor.getId())
                .averageRating(roundedRating)
                .totalReviewer(totalReviewer)
                .reviews(reviews.stream().map(this::mapToResponse).toList())
                .build();

    }


    private ReviewResponse mapToResponse(Review review) {
    return ReviewResponse.builder()
            .id(review.getId())
            .comment(review.getComment())
            .rating(review.getRating())
            .tutorId(review.getTutor().getId())
            .studentId(review.getStudent().getId())
            .createdAt(review.getCreateAt())
            .build();
    }



}
