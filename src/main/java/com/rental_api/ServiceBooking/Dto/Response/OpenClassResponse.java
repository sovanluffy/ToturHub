package com.rental_api.ServiceBooking.Dto.Response;

import lombok.*;
import java.math.BigDecimal;
import java.util.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpenClassResponse {
    private Long classId;
    private String title;
    private String description;
    private String status;
    
    // Tutor Info
    private Long tutorId;
    private String tutorName;
    private String tutorImage; 
    private Double tutorRating;
    private Integer yearsOfExperience;

    // Location Info
    private String location; // Formatted as "District, City"
    private String specificAddress;

    // Data Lists
    private List<String> subjects;
    private Set<String> learningModes;
    private Map<Integer, BigDecimal> pricing;
    private List<ScheduleDto> availableSlots;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScheduleDto {
        private Long id;
        private String timeRange;
        private boolean isBooked;
    }
}