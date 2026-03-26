package com.rental_api.ServiceBooking.Services;

import com.rental_api.ServiceBooking.Dto.Request.OpenClassRequest;
import com.rental_api.ServiceBooking.Dto.Response.OpenClassResponse;
import com.rental_api.ServiceBooking.Dto.Response.TutorCardResponse;

import java.util.List;

public interface OpenClassService {
    
    /**
     * Create a new class offering with pricing and time slots.
     */
    OpenClassResponse createClass(OpenClassRequest request);

    /**
     * Search for all classes with status 'OPEN'. 
     * @param city Optional filter for location (e.g., "Phnom Penh").
     */
    List<OpenClassResponse> findAllActiveClasses(String city);

    /**
     * Get the full details of a specific class including all available schedules.
     */
    OpenClassResponse getClassDetails(Long id);

    /**
     * Get basic Tutor cards for the main discovery page.
     */
    List<TutorCardResponse> getAllPublicCards();
}