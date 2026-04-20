package com.rental_api.ServiceBooking.Repository;

import com.rental_api.ServiceBooking.Entity.BookingClass;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRepository extends JpaRepository<BookingClass, Long> {

    List<BookingClass> findByUserId(Long userId);

    List<BookingClass> findByOpenClassId(Long classId);

    // ✅ FIXED (IMPORTANT)
    List<BookingClass> findByOpenClass_Tutor_Id(Long tutorId);

    boolean existsByUserIdAndScheduleId(Long userId, Long scheduleId);
}