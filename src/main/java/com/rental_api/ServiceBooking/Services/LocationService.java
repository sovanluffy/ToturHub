package com.rental_api.ServiceBooking.Service;

import com.rental_api.ServiceBooking.Dto.Response.LocationResponse;
import java.util.List;

public interface LocationService {
    // Get all locations for the signup dropdown
    List<LocationResponse> getAllLocations();
    
    // Get a single location by ID
    LocationResponse getLocationById(Long id);
    
    // Filter by city
    List<LocationResponse> getLocationsByCity(String city);
}