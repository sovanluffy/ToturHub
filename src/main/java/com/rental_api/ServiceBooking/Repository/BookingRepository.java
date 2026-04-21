package com.rental_api.ServiceBooking.Repository;

import com.rental_api.ServiceBooking.Entity.BookingClass;
import com.rental_api.ServiceBooking.Entity.Enum.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookingRepository extends JpaRepository<BookingClass, Long> {

    // ================= USER =================
    List<BookingClass> findByUser_Id(Long userId);

    boolean existsByUser_IdAndSchedule_Id(Long userId, Long scheduleId);

    // ================= CLASS =================
    List<BookingClass> findByOpenClass_Id(Long classId);

    // ================= TUTOR =================
    List<BookingClass> findByTutor_Id(Long tutorId);

    // 🔥 COUNT PENDING BOOKINGS (IMPORTANT FOR BADGE)
    long countByTutor_IdAndStatus(Long tutorId, BookingStatus status);

    // Optional JPQL (backup)
    @Query("SELECT b FROM BookingClass b WHERE b.tutor.id = :tutorId")
    List<BookingClass> findTutorBookings(@Param("tutorId") Long tutorId);
}