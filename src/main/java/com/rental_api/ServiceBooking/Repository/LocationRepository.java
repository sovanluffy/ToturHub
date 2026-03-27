package com.rental_api.ServiceBooking.Repository;

import com.rental_api.ServiceBooking.Entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
    
    // Custom query to find locations by city (e.g., all districts in Phnom Penh)
    List<Location> findByCity(String city);
    
    // Custom query to find a specific district
    List<Location> findByDistrict(String district);
}