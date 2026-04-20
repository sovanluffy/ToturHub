package com.rental_api.ServiceBooking.Repository;

import com.rental_api.ServiceBooking.Entity.DayTimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DayTimeSlotRepository extends JpaRepository<DayTimeSlot, Long> {
}