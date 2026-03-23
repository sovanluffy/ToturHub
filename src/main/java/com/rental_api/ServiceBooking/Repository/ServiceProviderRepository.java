package com.rental_api.ServiceBooking.Repository;

import com.rental_api.ServiceBooking.Entity.ServiceProvider;
import com.rental_api.ServiceBooking.Entity.User;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceProviderRepository extends JpaRepository<ServiceProvider, Long> {
     long countByUserId(Long userId);
      Optional<ServiceProvider> findByUser(User user);

    // Or find by user ID directly
    Optional<ServiceProvider> findByUserId(Long userId);
}
