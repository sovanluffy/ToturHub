package com.rental_api.ServiceBooking.Repository;

import com.rental_api.ServiceBooking.Entity.Tutor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TutorRepository extends JpaRepository<Tutor, Long> {
    Optional<Tutor> findByUserId(Long userId);
}