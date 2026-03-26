package com.rental_api.ServiceBooking.Services;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;

import com.rental_api.ServiceBooking.Dto.Request.TutorProfileRequest;
import com.rental_api.ServiceBooking.Dto.Response.TutorFullViewResponse;

public interface TutorService {
    
    /**
     * Save or Update the professional identity.
     * Keeps the profile as a "Draft" (isPublic = false) until published.
     */
    void updateTutorProfile(
        TutorProfileRequest request, 
        MultipartFile profileImg, 
        MultipartFile videoFile, 
        List<MultipartFile> certs
    );

    /**
     * Makes the tutor profile visible on the public website.
     * Flips the isPublic flag to true.
     */
    void publishProfile();

    /**
     * Hides the tutor profile from the public website.
     * Flips the isPublic flag to false.
     */
    void unpublishProfile();

    /**
     * ✅ NEW: Get the logged-in tutor's own profile.
     * Identified via Security Token. Used for the private dashboard.
     */
    TutorFullViewResponse getMyOwnProfile();

    /**
     * Get the "See All" view for a specific tutor (Public or Admin view).
     */
    TutorFullViewResponse getTutorFullDetail(Long tutorId);
    
    /**
     * Increment stats when a student finishes a class.
     */
    void incrementStudentCount(Long tutorId);
}