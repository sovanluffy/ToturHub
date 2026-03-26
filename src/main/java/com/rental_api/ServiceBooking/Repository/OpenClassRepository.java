package com.rental_api.ServiceBooking.Repository;

import com.rental_api.ServiceBooking.Entity.OpenClass;
import com.rental_api.ServiceBooking.Entity.OpenClass.ClassStatus;
import com.rental_api.ServiceBooking.Entity.OpenClass.LearningMode;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OpenClassRepository extends JpaRepository<OpenClass, Long> {

    // CHANGE THIS: 
    // From: findByLearningModeAndStatus
    // To:   findByLearningModesAndStatus
    List<OpenClass> findByLearningModesAndStatus(LearningMode mode, ClassStatus status);

    List<OpenClass> findByCityAndStatus(String city, ClassStatus status);
    
    List<OpenClass> findByTutorId(Long tutorId);
}