package com.rental_api.ServiceBooking.Repository;

import com.rental_api.ServiceBooking.Entity.BookingClass;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRepository extends JpaRepository<BookingClass, Long> {

    List<BookingClass> findByUserId(Long userId);

    List<BookingClass> findByOpenClassId(Long classId);

    List<BookingClass> findByTutorId(Long tutorId);

    // 🔥 FIX: prevent duplicate booking
    boolean existsByUserIdAndScheduleId(Long userId, Long scheduleId);
}