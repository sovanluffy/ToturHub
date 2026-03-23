package com.rental_api.ServiceBooking.Repository;

import com.rental_api.ServiceBooking.Entity.ProviderRequest;
import com.rental_api.ServiceBooking.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProviderRequestRepository extends JpaRepository<ProviderRequest, Long> {
    List<ProviderRequest> findByUser(User user);
    boolean existsByUserIdAndStatus(Long userId, String status);
}
