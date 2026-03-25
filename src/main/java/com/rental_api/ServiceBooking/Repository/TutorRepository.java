package com.rental_api.ServiceBooking.Repository;

import com.rental_api.ServiceBooking.Entity.Tutor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TutorRepository extends JpaRepository<Tutor, Long> {
    // Custom query to find tutors by a specific subject name
    List<Tutor> findBySubjects_NameIgnoreCase(String subjectName);
}


