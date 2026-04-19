package com.rental_api.ServiceBooking.Repository;

import com.rental_api.ServiceBooking.Entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubjectRepository extends JpaRepository<Subject, Long> {

    boolean existsByName(String name);
}