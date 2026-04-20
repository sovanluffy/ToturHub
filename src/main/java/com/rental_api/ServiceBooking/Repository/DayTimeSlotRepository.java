package com.rental_api.ServiceBooking.Repository;

import com.rental_api.ServiceBooking.Entity.DayTimeSlot;

import jakarta.persistence.LockModeType;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DayTimeSlotRepository extends JpaRepository<DayTimeSlot, Long> {
     // 🔥 FIX DOUBLE BOOKING (PESSIMISTIC LOCK)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM DayTimeSlot d WHERE d.id = :id")
    Optional<DayTimeSlot> findByIdForUpdate(@Param("id") Long id);
}