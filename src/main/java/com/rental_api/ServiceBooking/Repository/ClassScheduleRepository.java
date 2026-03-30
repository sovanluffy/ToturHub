package com.rental_api.ServiceBooking.Repository;

import com.rental_api.ServiceBooking.Entity.ClassSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ClassScheduleRepository extends JpaRepository<ClassSchedule, Long> {
    
    // Find all available slots for a specific class
    List<ClassSchedule> findByOpenClassIdAndIsBookedFalse(Long openClassId);
    
    // Find all slots for a specific tutor
    List<ClassSchedule> findByOpenClassTutorId(Long tutorId);
}