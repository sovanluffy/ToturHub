package com.rental_api.ServiceBooking.Dto.Response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TutorFullViewResponse {

    // ================= PROFILE =================
    private Long tutorId;
    private String fullname;
    private String bio;
    private String profilePicture;
    private String introVideoUrl;
    private List<String> certificateImages;

    private Double rating;
    private Integer studentsTaught;
    private boolean isPublic;

    // ================= TIMELINE =================
    private List<EducationDto> education;
    private List<ExperienceDto> experience;

    // ================= CONTENT =================
    private List<ClassSummaryDto> activeClasses;
    private List<ReviewDto> reviews;

    // ================= DTOS =================

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

    // ================= FIXED CLASS DTO =================
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClassSummaryDto {

        private Long id;
        private String title;
        private String description;

        private String classImage;
        private String status;

        private String location;
        private BigDecimal basePrice;

        private Set<String> modes;
    }
}