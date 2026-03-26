package com.rental_api.ServiceBooking.Services;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;

import com.rental_api.ServiceBooking.Dto.Request.TutorProfileRequest;
import com.rental_api.ServiceBooking.Dto.Response.TutorFullViewResponse;

public interface TutorService {
    
    /**
     * Save or Update the professional identity.
     * Handles Bio, Education, and Experience via JSON, 
     * and handles Profile Image, Intro Video, and Certificates via Local File Storage.
     */
    void updateTutorProfile(
        TutorProfileRequest request, 
        MultipartFile profileImg, 
        MultipartFile videoFile, 
        List<MultipartFile> certs
    );

    /**
     * Get the "See All" view: Profile + All Posted Classes + Reviews
     */
    TutorFullViewResponse getTutorFullDetail(Long tutorId);
    
    /**
     * Increment stats when a student finishes a class
     */
    void incrementStudentCount(Long tutorId);
}