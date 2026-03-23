package com.rental_api.ServiceBooking.Services.impl;

import com.rental_api.ServiceBooking.Dto.Request.ServiceRequest;
import com.rental_api.ServiceBooking.Dto.Response.ServiceResponse;
import com.rental_api.ServiceBooking.Entity.Category;
import com.rental_api.ServiceBooking.Entity.ServiceEntity;
import com.rental_api.ServiceBooking.Entity.ServiceProvider;
import com.rental_api.ServiceBooking.Exception.ServiceNotFoundException;
import com.rental_api.ServiceBooking.Repository.CategoryRepository;
import com.rental_api.ServiceBooking.Repository.ServiceProviderRepository;
import com.rental_api.ServiceBooking.Repository.ServiceRepository;
import com.rental_api.ServiceBooking.Security.JwtUtils;
import com.rental_api.ServiceBooking.Services.ServiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServiceServiceImpl implements ServiceService {

    private final ServiceRepository serviceRepository;
    private final ServiceProviderRepository providerRepository;
    private final CategoryRepository categoryRepository;
    private final JwtUtils jwtUtils;

    // ---------------- CREATE SERVICE ----------------
    @Override
    public ServiceResponse createService(ServiceRequest request, String token) {
        // Extract userId from JWT
        Long userId = jwtUtils.extractUserId(token);

        // Find provider by userId
        ServiceProvider provider = providerRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Provider not found for this user"));

        // Find category
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        // Build service entity
        ServiceEntity service = ServiceEntity.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .duration(request.getDuration())
                .provider(provider)
                .category(category)
                .imageUrl(request.getImageUrl())
                .build();

        // Save
        serviceRepository.save(service);

        // Map and return response
        return mapToResponse(service);
    }

    // ---------------- READ ALL ----------------
    @Override
    public List<ServiceResponse> getAllServices() {
        return serviceRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ---------------- READ BY ID ----------------
    @Override
    public ServiceResponse getServiceById(Long id) {
        ServiceEntity service = serviceRepository.findById(id)
                .orElseThrow(() -> new ServiceNotFoundException("Service with ID " + id + " not found"));
        return mapToResponse(service);
    }

    // ---------------- UPDATE ----------------
    @Override
    public ServiceResponse updateService(Long id, ServiceRequest request) {
        ServiceEntity service = serviceRepository.findById(id)
                .orElseThrow(() -> new ServiceNotFoundException("Service with ID " + id + " not found"));

        service.setName(request.getName());
        service.setDescription(request.getDescription());
        service.setPrice(request.getPrice());
        service.setDuration(request.getDuration());
        service.setImageUrl(request.getImageUrl());

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            service.setCategory(category);
        }

        serviceRepository.save(service);
        return mapToResponse(service);
    }

    // ---------------- DELETE ----------------
    @Override
    public void deleteService(Long id) {
        ServiceEntity service = serviceRepository.findById(id)
                .orElseThrow(() -> new ServiceNotFoundException("Service with ID " + id + " not found"));
        serviceRepository.delete(service);
    }

    // ---------------- MAPPER ----------------
    private ServiceResponse mapToResponse(ServiceEntity service) {
        return ServiceResponse.builder()
                .id(service.getId())
                .name(service.getName())
                .description(service.getDescription())
                .price(service.getPrice())
                .duration(service.getDuration())
                .categoryName(service.getCategory().getName())
                .providerName(service.getProvider().getUser().getFullname())
                .imageUrl(service.getImageUrl())
                .build();
    }
}
