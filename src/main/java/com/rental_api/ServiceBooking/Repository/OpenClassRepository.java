package com.rental_api.ServiceBooking.Repository;

import com.rental_api.ServiceBooking.Entity.OpenClass;
import com.rental_api.ServiceBooking.Entity.OpenClass.LearningMode;
import com.rental_api.ServiceBooking.Entity.OpenClass.ClassStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OpenClassRepository extends JpaRepository<OpenClass, Long> {
    List<OpenClass> findByCityIgnoreCaseAndStatus(String city, ClassStatus status);
    List<OpenClass> findByLearningModeAndStatus(LearningMode mode, ClassStatus status);
}