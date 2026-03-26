package com.rental_api.ServiceBooking.Services;

import com.rental_api.ServiceBooking.Dto.Request.OpenClassRequest;
import com.rental_api.ServiceBooking.Dto.Response.OpenClassResponse;
import com.rental_api.ServiceBooking.Dto.Response.TutorCardResponse;

import java.util.List;

public interface OpenClassService {
    
    // Your existing method
    OpenClassResponse createClass(OpenClassRequest request);

    // --- ADD THIS LINE TO FIX THE ERROR ---
    List<OpenClassResponse> findAllActiveClasses(String city);

    // If you implemented this in the Impl, add it here too:
    OpenClassResponse getClassDetails(Long id);
    List<TutorCardResponse> getAllPublicCards();
}