package com.rental_api.ServiceBooking.Repository;

import com.rental_api.ServiceBooking.Entity.Tutor;
import com.rental_api.ServiceBooking.Dto.Response.TutorCardResponse; // Ensure this DTO exists
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface TutorRepository extends JpaRepository<Tutor, Long> {
    
    Optional<Tutor> findByUserId(Long userId);
    
    Optional<Tutor> findByUserEmail(String email);

    /**
     * ✅ FIX: This query joins the User table (u) with the Tutor table (t)
     * to pull the avatarUrl directly into the DTO.
     */
    @Query("SELECT new com.rental_api.ServiceBooking.Dto.Response.TutorCardResponse(" +
           "t.id, u.fullname, u.avatarUrl, t.averageRating, t.totalStudentsTaught, t.bio) " +
           "FROM Tutor t JOIN t.user u WHERE t.isPublic = true")
    List<TutorCardResponse> findAllPublicTutorCards();
}