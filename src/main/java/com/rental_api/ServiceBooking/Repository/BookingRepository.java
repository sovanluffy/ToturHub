package com.rental_api.ServiceBooking.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rental_api.ServiceBooking.Entity.BookingClass;

@Repository
public interface BookingRepository extends JpaRepository<BookingClass, Long> {

}
