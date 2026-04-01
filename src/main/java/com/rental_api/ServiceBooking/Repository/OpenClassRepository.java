package com.rental_api.ServiceBooking.Repository;

import com.rental_api.ServiceBooking.Entity.OpenClass;
import com.rental_api.ServiceBooking.Entity.OpenClass.ClassStatus;
import com.rental_api.ServiceBooking.Entity.OpenClass.LearningMode;
import com.rental_api.ServiceBooking.Entity.Tutor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OpenClassRepository extends JpaRepository<OpenClass, Long>, JpaSpecificationExecutor<OpenClass> {

    /**
     * ✅ FIX: Use "Location_City" to tell Spring to look inside the Location entity.
     * Logic: OpenClass -> location -> city
     */
    List<OpenClass> findByLocation_CityAndStatus(String city, ClassStatus status);

    /**
     * ✅ FIX: Use "Location_District" for nested search.
     */
    List<OpenClass> findByLocation_DistrictAndStatus(String district, ClassStatus status);

    /**
     * ✅ Corrected: Find by LearningMode in a Set
     */
    List<OpenClass> findByLearningModesContainingAndStatus(LearningMode mode, ClassStatus status);
    
    /**
     * ✅ Standard find by Tutor ID
     */
    List<OpenClass> findByTutorId(Long tutorId);
      // ✅ Add this method for getting all classes by a list of tutors
    List<OpenClass> findAllByTutorIn(List<Tutor> tutors);
}