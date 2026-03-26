package com.rental_api.ServiceBooking.Services;

import com.rental_api.ServiceBooking.Dto.Request.OpenClassRequest;
import com.rental_api.ServiceBooking.Dto.Response.OpenClassResponse;
import java.util.List;

public interface OpenClassService {
    // 1. Tutor opens a class with multiple prices and time slots
    OpenClassResponse createClass(OpenClassRequest request);

    // 2. Student chooses location: City, District, and Learning Mode
    List<OpenClassResponse> searchClasses(String city, String district, String learningMode);

    // 3. Get all details for a specific class (including clickable slots)
    OpenClassResponse getClassDetails(Long id);
}