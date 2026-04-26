package com.rental_api.ServiceBooking.Services.impl;

import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rental_api.ServiceBooking.Dto.Request.ReviewRequest;
import com.rental_api.ServiceBooking.Dto.Response.GetReviewResponse;
import com.rental_api.ServiceBooking.Dto.Response.ReviewResponse;
import com.rental_api.ServiceBooking.Entity.Review;
import com.rental_api.ServiceBooking.Entity.Tutor;
import com.rental_api.ServiceBooking.Entity.User;
import com.rental_api.ServiceBooking.Entity.OpenClass;
import com.rental_api.ServiceBooking.Entity.Enum.BookingStatus;
import com.rental_api.ServiceBooking.Exception.ResourceNotFoundException;
import com.rental_api.ServiceBooking.Repository.BookingRepository;
import com.rental_api.ServiceBooking.Repository.ReviewRepository;
import com.rental_api.ServiceBooking.Repository.TutorRepository;
import com.rental_api.ServiceBooking.Repository.UserRepository;
import com.rental_api.ServiceBooking.Repository.OpenClassRepository;
import com.rental_api.ServiceBooking.Services.ReviewService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final TutorRepository tutorRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final OpenClassRepository openClassRepository;

    // =====================================================
    // ================= CURRENT USER ======================
    // =====================================================

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    // =====================================================
    // ================= CREATE CLASS REVIEW ================
    // =====================================================

    @Override
    @Transactional
    public ReviewResponse createClassReview(Long classId, ReviewRequest reviewRequest) {

        User currentUser = getCurrentUser();

        // 1. Load the specific class to review
        OpenClass openClass = openClassRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + classId));

        Tutor tutor = openClass.getTutor();

        // 2. VALIDATION: Check if student has a CONFIRMED booking for THIS SPECIFIC CLASS
        boolean hasConfirmedBooking =
                bookingRepository.existsByUser_IdAndOpenClass_IdAndStatus(
                        currentUser.getId(),
                        classId,
                        BookingStatus.CONFIRMED
                );

        if (!hasConfirmedBooking) {
            throw new IllegalStateException(
                    "Action Denied: You can only review classes you have a confirmed booking for."
            );
        }

        // 3. DUPLICATE CHECK: Prevent multiple reviews for the same class by the same student
        boolean alreadyReviewed =
                reviewRepository.existsByStudent_IdAndOpenClass_Id(currentUser.getId(), classId);

        if (alreadyReviewed) {
            throw new IllegalStateException("Duplicate Action: You have already reviewed this class.");
        }

        // 4. SAVE REVIEW
        Review review = Review.builder()
                .comment(reviewRequest.getComment())
                .rating(reviewRequest.getRating())
                .tutor(tutor)
                .student(currentUser)
                .openClass(openClass) 
                .build();

        Review saved = reviewRepository.save(review);
        
        // Update tutor's global average rating (Recalculates based on all reviews)
        updateTutorGlobalRating(tutor.getId());

        return mapToResponse(saved);
    }

    // =====================================================
    // ================= GET REVIEWS BY CLASS ===============
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public GetReviewResponse getReviewsByClassId(Long classId) {

        OpenClass openClass = openClassRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found"));

        // Get reviews specifically for this class
        List<Review> reviews = reviewRepository.findByOpenClass_Id(classId);

        double averageRating = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        // Format to 1 decimal place (e.g., 4.5)
        double roundedRating = Math.round(averageRating * 10.0) / 10.0;

        return GetReviewResponse.builder()
                .tutorId(openClass.getTutor().getId())
                .classId(classId)
                .classTitle(openClass.getTitle())
                .averageRating(roundedRating)
                .totalReviewer(reviews.size()) // "Total People" count for this class
                .reviews(reviews.stream().map(this::mapToResponse).toList())
                .build();
    }

    // =====================================================
    // ================= GET REVIEWS BY TUTOR ===============
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public GetReviewResponse getReviewByTutorId(Long tutorId) {
        
        if (!tutorRepository.existsById(tutorId)) {
            throw new ResourceNotFoundException("Tutor not found");
        }

        // Get all reviews for this tutor across all their classes
        List<Review> reviews = reviewRepository.findByTutorId(tutorId);
        
        double averageRating = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
        
        return GetReviewResponse.builder()
                .tutorId(tutorId)
                .averageRating(Math.round(averageRating * 10.0) / 10.0)
                .totalReviewer(reviews.size()) // "Total People" count for this tutor
                .reviews(reviews.stream().map(this::mapToResponse).toList())
                .build();
    }

    // =====================================================
    // ================= HELPERS & MAPPING =================
    // =====================================================

    /**
     * Recalculates the tutor's average rating from all reviews and saves it.
     */
    private void updateTutorGlobalRating(Long tutorId) {
        Tutor tutor = tutorRepository.findById(tutorId).orElseThrow();
        Double avg = reviewRepository.getAverageRatingByTutorId(tutorId);
        tutor.setAverageRating(avg != null ? avg : 0.0);
        tutorRepository.save(tutor);
    }

    private ReviewResponse mapToResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .comment(review.getComment())
                .rating(review.getRating())
                .tutorId(review.getTutor().getId())
                .studentId(review.getStudent().getId())
                .studentName(review.getStudent().getFullname())
                .studentAvatar(review.getStudent().getAvatarUrl())
                .classId(review.getOpenClass() != null ? review.getOpenClass().getId() : null)
                .createdAt(review.getCreateAt())
                .build();
    }

    /**
     * Legacy support: Redirects or throws error if the old generic method is called.
     */
    @Override
    public ReviewResponse createReview(Long tutorId, ReviewRequest reviewRequest) {
        throw new UnsupportedOperationException("Error: Use createClassReview(Long classId, ...) to submit feedback.");
    }
}