package com.rental_api.ServiceBooking.Repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.rental_api.ServiceBooking.Entity.BookingClass;

@Repository
public interface BookingRepository extends JpaRepository<BookingClass, Long> {
    // Finds all bookings for a specific class (useful for tutors)
    List<BookingClass> findByOpenClassId(Long openClassId);

    // Finds all bookings made by a specific student
    List<BookingClass> findByUserId(Long userId);
    
    List<BookingClass> findByTutorId(Long tutorId); // Add this line!
}