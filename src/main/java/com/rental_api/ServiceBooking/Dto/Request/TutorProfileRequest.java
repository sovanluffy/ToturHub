package com.rental_api.ServiceBooking.Dto.Request;

import lombok.Data;
import java.util.List;

@Data
public class TutorProfileRequest {
    private Long tutorId;
    private String bio;
    private String profilePicture;
    private String introVideoUrl;
    private List<String> certificateImages;
    
    private List<EducationRequest> education;
    private List<ExperienceRequest> experience;

    @Data public static class EducationRequest { String school, degree, year; }
    @Data public static class ExperienceRequest { String company, role, duration; }
}