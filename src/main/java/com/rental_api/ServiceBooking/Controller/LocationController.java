package com.rental_api.ServiceBooking.Controller;

import com.rental_api.ServiceBooking.Dto.Response.LocationResponse;
import com.rental_api.ServiceBooking.Service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/locations")
@RequiredArgsConstructor
// Allow your React/Vite frontend to access this API
@CrossOrigin(origins = "*") 
public class LocationController {

    private final LocationService locationService;

    /**
     * GET /api/v1/locations
     * Used by the Frontend to populate the signup dropdown
     */
    @GetMapping
    public ResponseEntity<List<LocationResponse>> getAllLocations() {
        List<LocationResponse> locations = locationService.getAllLocations();
        return ResponseEntity.ok(locations);
    }

    /**
     * GET /api/v1/locations/{id}
     * Get details for one specific location
     */
    @GetMapping("/{id}")
    public ResponseEntity<LocationResponse> getLocationById(@PathVariable Long id) {
        return ResponseEntity.ok(locationService.getLocationById(id));
    }

    /**
     * GET /api/v1/locations/city/{cityName}
     * Filter locations by city (e.g., /api/v1/locations/city/Phnom Penh)
     */
    @GetMapping("/city/{cityName}")
    public ResponseEntity<List<LocationResponse>> getLocationsByCity(@PathVariable String cityName) {
        return ResponseEntity.ok(locationService.getLocationsByCity(cityName));
    }
}