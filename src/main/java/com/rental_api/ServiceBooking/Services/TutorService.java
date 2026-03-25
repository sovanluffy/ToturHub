package com.rental_api.ServiceBooking.Services;

import com.rental_api.ServiceBooking.Dto.Request.TutorProfileRequest;
import com.rental_api.ServiceBooking.Dto.Response.TutorFullViewResponse;

public interface TutorService {
    // Save or Update the professional identity (Bio, Video, Education, Experience)
    void updateTutorProfile(TutorProfileRequest request);

    // Get the "See All" view: Profile + All Posted Classes + Reviews
    TutorFullViewResponse getTutorFullDetail(Long tutorId);
    
    // Increment stats when a student finishes a class
    void incrementStudentCount(Long tutorId);
}