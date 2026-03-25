package com.rental_api.ServiceBooking.Repository;

import com.rental_api.ServiceBooking.Entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

// --- SubjectRepository ---
@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {
    java.util.Optional<Subject> findByName(String name);
}