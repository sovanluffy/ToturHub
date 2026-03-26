package com.rental_api.ServiceBooking.Services;

import com.rental_api.ServiceBooking.Dto.Request.OpenClassRequest;
import com.rental_api.ServiceBooking.Dto.Response.OpenClassResponse;
import com.rental_api.ServiceBooking.Dto.Response.TutorCardResponse;

import java.math.BigDecimal;
import java.util.List;

public interface OpenClassService {
    
    /**
     * Create a new class offering. The Tutor ID is extracted from the Security Token.
     */
    OpenClassResponse createClass(OpenClassRequest request);

    /**
     * Advanced search using JPA Specifications.
     */
    List<OpenClassResponse> searchClasses(
            String city, 
            Long subjectId, 
            BigDecimal maxPrice, 
            Integer minExp
    );

    /**
     * Get details for a specific class listing.
     */
    OpenClassResponse getClassDetails(Long id);

    /**
     * Get all public tutor cards for the discovery/home page.
     */
    List<TutorCardResponse> getAllPublicCards();

    /**
     * Get all classes created by a specific tutor.
     */
    List<OpenClassResponse> findByTutorId(Long tutorId);
}