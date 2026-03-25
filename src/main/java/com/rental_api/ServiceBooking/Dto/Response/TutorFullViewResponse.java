package com.rental_api.ServiceBooking.Dto.Response;

import lombok.*;
import java.math.BigDecimal;
import java.util.*;

@Data
@Builder
public class TutorFullViewResponse {
    // --- Profile Section ---
    private Long tutorId;
    private String fullname;
    private String bio;
    private String profilePicture;
    private String videoUrl;
    private List<String> certificates;
    private Double rating;
    private Integer studentsTaught;

    // --- History Timelines ---
    private List<EducationDto> education;
    private List<ExperienceDto> experience;

    // --- "Post More" Section: List of all classes ---
    private List<ClassSummaryDto> activeClasses;

    // --- Social Proof ---
    private List<ReviewDto> reviews;

    @Data @Builder public static class EducationDto { String school, degree, year; }
    @Data @Builder public static class ExperienceDto { String company, role, duration; }
    @Data @Builder public static class ReviewDto { String student, comment; Integer stars; }
    
    @Data @Builder public static class ClassSummaryDto {
        private Long id;
        private String title;
        private Map<Integer, BigDecimal> prices;
        private Set<String> modes;
    }
}