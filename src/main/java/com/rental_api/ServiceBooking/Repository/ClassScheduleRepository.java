package com.rental_api.ServiceBooking.Repository;

import com.rental_api.ServiceBooking.Entity.ClassSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ClassScheduleRepository extends JpaRepository<ClassSchedule, Long> {

    // ================= AVAILABLE SLOTS =================
    List<ClassSchedule> findByBookedFalse();

    // ================= BOOKED SLOTS =================
    List<ClassSchedule> findByBookedTrue();

    // ================= BY DATE =================
    List<ClassSchedule> findByStartDate(LocalDate startDate);

    List<ClassSchedule> findByEndDate(LocalDate endDate);
}