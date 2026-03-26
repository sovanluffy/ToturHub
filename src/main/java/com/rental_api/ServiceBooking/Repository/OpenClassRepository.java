package com.rental_api.ServiceBooking.Repository;

import com.rental_api.ServiceBooking.Entity.OpenClass;
import com.rental_api.ServiceBooking.Entity.OpenClass.ClassStatus;
import com.rental_api.ServiceBooking.Entity.OpenClass.LearningMode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OpenClassRepository extends JpaRepository<OpenClass, Long>, JpaSpecificationExecutor<OpenClass> {

    /**
     * ✅ Corrected: Find classes by a specific LearningMode inside the Set.
     * Use "Containing" when dealing with Collections (Set/List) in method names.
     */
    List<OpenClass> findByLearningModesContainingAndStatus(LearningMode mode, ClassStatus status);

    /**
     * Find all open classes in a specific city.
     */
    List<OpenClass> findByCityAndStatus(String city, ClassStatus status);
    
    /**
     * Get all classes created by a specific tutor.
     */
    List<OpenClass> findByTutorId(Long tutorId);

    /**
     * ✅ IMPORTANT: Adding JpaSpecificationExecutor allows you to pass 
     * the Specification we built for Subject, Price, and Experience filters.
     */
}