package com.rental_api.ServiceBooking.Repository;

import com.rental_api.ServiceBooking.Dto.Response.TutorCardResponse;
import com.rental_api.ServiceBooking.Entity.Tutor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface TutorRepository extends JpaRepository<Tutor, Long> {

    Optional<Tutor> findByUserId(Long userId);

    Optional<Tutor> findByUserEmail(String email);

    // ✅ THE FIX: Fetches image from Media table first, falls back to User avatar
    @Query("SELECT new com.rental_api.ServiceBooking.Dto.Response.TutorCardResponse(" +
           "t.id, " +
           "u.fullname, " +
           "COALESCE(m.profileImageUrl, u.avatarUrl), " + 
           "t.averageRating, " +
           "t.totalStudentsTaught, " +
           "t.bio) " +
           "FROM Tutor t " +
           "JOIN t.user u " +
           "LEFT JOIN t.media m " + 
           "WHERE t.isPublic = true")
    List<TutorCardResponse> findAllPublicTutorCards();
}