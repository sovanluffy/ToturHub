package com.rental_api.ServiceBooking.Dto.Response;

import lombok.*;
import java.math.BigDecimal;
import java.util.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TutorFullViewResponse {
    // --- Profile Section ---
    private Long tutorId;
    private String fullname;
    private String bio;
    private String profilePicture;

    // ✅ CHANGED: Renamed to match the Service Builder
    private String introVideoUrl; 
    
    // ✅ CHANGED: Renamed to match the Service Builder
    private List<String> certificateImages; 

    private Double rating;
    private Integer studentsTaught;

    // --- History Timelines ---
    private List<EducationDto> education;
    private List<ExperienceDto> experience;

    // --- Sections ---
    private List<ClassSummaryDto> activeClasses;
    private List<ReviewDto> reviews;

    @Data 
    @Builder 
    @NoArgsConstructor 
    @AllArgsConstructor 
    public static class EducationDto { 
        private String school; 
        private String degree; 
        private String year; 
    }

    @Data 
    @Builder 
    @NoArgsConstructor 
    @AllArgsConstructor 
    public static class ExperienceDto { 
        private String company; 
        private String role; 
        private String duration; 
    }

    @Data 
    @Builder 
    @NoArgsConstructor 
    @AllArgsConstructor 
    public static class ReviewDto { 
        private String student; 
        private String comment; 
        private Integer stars; 
    }
    
    @Data 
    @Builder 
    @NoArgsConstructor 
    @AllArgsConstructor 
    public static class ClassSummaryDto {
        private Long id;
        private String title;
        private Map<Integer, BigDecimal> prices;
        private Set<String> modes;
    }
}