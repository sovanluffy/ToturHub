package com.rental_api.ServiceBooking.Repository;

import com.rental_api.ServiceBooking.Entity.Review;
import com.rental_api.ServiceBooking.Entity.Tutor;
import com.rental_api.ServiceBooking.Entity.User;
import com.rental_api.ServiceBooking.Entity.OpenClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // =========================================================
    // 🏫 CLASS-SPECIFIC METHODS
    // =========================================================

    // Find all reviews for a specific class
    List<Review> findByOpenClass_Id(Long classId);

    // Count total number of people who reviewed a specific class
    long countByOpenClass_Id(Long classId);

    // Calculate Average Rating for a specific class
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.openClass.id = :classId")
    Double getAverageRatingByClassId(@Param("classId") Long classId);

    // Check if a student has already reviewed this specific class
    boolean existsByStudent_IdAndOpenClass_Id(Long studentId, Long classId);


    // =========================================================
    // 👤 TUTOR-LEVEL METHODS (Aggregated)
    // =========================================================

    // Find all reviews across all classes for a tutor
    List<Review> findByTutorId(Long tutorId);

    // Count total reviews across all classes for a tutor
    long countByTutorId(Long tutorId);

    // Calculate Tutor's overall Average Rating from all their classes
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.tutor.id = :tutorId")
    Double getAverageRatingByTutorId(@Param("tutorId") Long tutorId);

    
    // =========================================================
    // 🛡️ SECURITY & VALIDATION
    // =========================================================

    // Check if student exists as a reviewer for a specific tutor (General check)
    boolean existsByStudentAndTutor(User student, Tutor tutor);

    // Check if student has reviewed this tutor before using IDs
    boolean existsByStudent_IdAndTutor_Id(Long studentId, Long tutorId);
}