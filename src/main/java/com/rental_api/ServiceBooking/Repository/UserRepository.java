package com.rental_api.ServiceBooking.Repository;

import com.rental_api.ServiceBooking.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Find user by email (used for register & later login)
    Optional<User> findByEmail(String email);
    

    // Optional: check if email exists
    boolean existsByEmail(String email);

    // Optional: check if phone exists
    boolean existsByPhone(String phone);
    
}
