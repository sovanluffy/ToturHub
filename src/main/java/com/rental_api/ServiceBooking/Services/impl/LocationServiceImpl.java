package com.rental_api.ServiceBooking.Service.Impl;

import com.rental_api.ServiceBooking.Dto.Response.LocationResponse;
import com.rental_api.ServiceBooking.Entity.Location;
import com.rental_api.ServiceBooking.Repository.LocationRepository;
import com.rental_api.ServiceBooking.Service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService {

    private final LocationRepository locationRepository;

    @Override
    public List<LocationResponse> getAllLocations() {
        return locationRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public LocationResponse getLocationById(Long id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Location not found with id: " + id));
        return mapToResponse(location);
    }

    @Override
    public List<LocationResponse> getLocationsByCity(String city) {
        return locationRepository.findByCity(city)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Helper method to map Entity -> Response DTO
     * This matches the locationId field your frontend expects
     */
    private LocationResponse mapToResponse(Location location) {
        return LocationResponse.builder()
                .locationId(location.getId())
                .city(location.getCity())
                .district(location.getDistrict())
                .fullAddress(location.getAddress())
                .build();
    }
}