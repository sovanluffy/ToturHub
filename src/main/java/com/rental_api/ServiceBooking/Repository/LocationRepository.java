package com.rental_api.ServiceBooking.Repository;

import com.rental_api.ServiceBooking.Entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
    
    // Find all locations by city (e.g., "Phnom Penh")
    List<Location> findByCity(String city);
    
    // Find a specific district (e.g., "BKK1")
    List<Location> findByDistrict(String district);
}