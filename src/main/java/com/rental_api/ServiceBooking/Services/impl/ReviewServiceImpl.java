package com.rental_api.ServiceBooking.Services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rental_api.ServiceBooking.Dto.Request.ReviewRequest;
import com.rental_api.ServiceBooking.Dto.Response.GetReviewResponse;
import com.rental_api.ServiceBooking.Dto.Response.ReviewResponse;
import com.rental_api.ServiceBooking.Entity.Enum.BookingStatus;
import com.rental_api.ServiceBooking.Entity.OpenClass;
import com.rental_api.ServiceBooking.Entity.Review;
import com.rental_api.ServiceBooking.Entity.Tutor;
import com.rental_api.ServiceBooking.Entity.User;
import com.rental_api.ServiceBooking.Exception.ResourceNotFoundException;
import com.rental_api.ServiceBooking.Repository.BookingRepository;
import com.rental_api.ServiceBooking.Repository.OpenClassRepository;
import com.rental_api.ServiceBooking.Repository.ReviewRepository;
import com.rental_api.ServiceBooking.Repository.TutorRepository;
import com.rental_api.ServiceBooking.Repository.UserRepository;
import com.rental_api.ServiceBooking.Services.ReviewService;

import java.util.List;

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
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    // =====================================================
    // ================= CREATE CLASS REVIEW ================
    // =====================================================
    @Override
    @Transactional
    public ReviewResponse createClassReview(Long classId, ReviewRequest reviewRequest) {
        User currentUser = getCurrentUser();

        // Load class
        OpenClass openClass = openClassRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + classId));

        Tutor tutor = openClass.getTutor();

        // 1. Check if student has CONFIRMED booking for this class
        boolean hasConfirmedBooking = bookingRepository.existsByUser_IdAndOpenClass_IdAndStatus(
                currentUser.getId(), classId, BookingStatus.CONFIRMED);

        if (!hasConfirmedBooking) {
            throw new IllegalStateException("Action Denied: You can only review classes you have a confirmed booking for.");
        }

        // 2. Prevent duplicate review for the same class
        boolean alreadyReviewed = reviewRepository.existsByStudent_IdAndOpenClass_Id(
                currentUser.getId(), classId);

        if (alreadyReviewed) {
            throw new IllegalStateException("Duplicate Action: You have already reviewed this class.");
        }

        // 3. Save review
        Review review = Review.builder()
                .comment(reviewRequest.getComment())
                .rating(reviewRequest.getRating())
                .tutor(tutor)
                .student(currentUser)
                .openClass(openClass)
                .build();

        Review savedReview = reviewRepository.save(review);

        // Update tutor's average rating
        updateTutorGlobalRating(tutor.getId());

        return mapToResponse(savedReview);
    }

    // =====================================================
    // ================= CREATE TUTOR REVIEW ================
    // =====================================================
    @Override
    @Transactional
    public ReviewResponse createTutorReview(Long tutorId, ReviewRequest reviewRequest) {
        User currentUser = getCurrentUser();

        // Load tutor
        Tutor tutor = tutorRepository.findById(tutorId)
                .orElseThrow(() -> new ResourceNotFoundException("Tutor not found with id: " + tutorId));

        // 1. Prevent duplicate review for the same tutor
        boolean alreadyReviewed = reviewRepository.existsByStudent_IdAndTutor_Id(
                currentUser.getId(), tutorId);

        if (alreadyReviewed) {
            throw new IllegalStateException("Duplicate Action: You have already reviewed this tutor.");
        }

        // 2. Save review (openClass can be null for tutor-level reviews)
        Review review = Review.builder()
                .comment(reviewRequest.getComment())
                .rating(reviewRequest.getRating())
                .tutor(tutor)
                .student(currentUser)
                .build();

        Review savedReview = reviewRepository.save(review);

        // Update tutor's average rating
        updateTutorGlobalRating(tutor.getId());

        return mapToResponse(savedReview);
    }

    // =====================================================
    // ================= GET REVIEWS BY CLASS ===============
    // =====================================================
    @Override
    @Transactional(readOnly = true)
    public GetReviewResponse getReviewsByClassId(Long classId) {
        OpenClass openClass = openClassRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + classId));

        List<Review> reviews = reviewRepository.findByOpenClass_Id(classId);

        double averageRating = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        double roundedRating = Math.round(averageRating * 10.0) / 10.0;

        return GetReviewResponse.builder()
                .tutorId(openClass.getTutor().getId())
                .classId(classId)
                .classTitle(openClass.getTitle())
                .averageRating(roundedRating)
                .totalReviewer(reviews.size())
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
            throw new ResourceNotFoundException("Tutor not found with id: " + tutorId);
        }

        List<Review> reviews = reviewRepository.findByTutorId(tutorId);

        double averageRating = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        return GetReviewResponse.builder()
                .tutorId(tutorId)
                .averageRating(Math.round(averageRating * 10.0) / 10.0)
                .totalReviewer(reviews.size())
                .reviews(reviews.stream().map(this::mapToResponse).toList())
                .build();
    }

    // =====================================================
    // ================= HELPER METHODS ====================
    // =====================================================
    private void updateTutorGlobalRating(Long tutorId) {
        Tutor tutor = tutorRepository.findById(tutorId)
                .orElseThrow(() -> new ResourceNotFoundException("Tutor not found with id: " + tutorId));

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

    // Legacy method - kept for compatibility but discouraged
    @Override
    public ReviewResponse createReview(Long tutorId, ReviewRequest reviewRequest) {
        throw new UnsupportedOperationException(
                "Error: Use createTutorReview(Long tutorId, ...) or createClassReview(Long classId, ...) instead."
        );
    }
}