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
    private Double tutorRating;

    // Location Info
    private String location; // Formatted as "District, City"
    private String specificAddress;

    // Data Lists
    private List<String> subjects;
    private Set<String> learningModes;
    private BigDecimal basePrice;
    private Integer maxStudents;
    private Integer currentStudents;
    private List<PriceTierDto> priceOptions;
    private List<ScheduleDto> availableSlots;

    // Inside OpenClassResponse.java


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScheduleDto {
        private Long id;
        private String timeRange;
        private Integer availableSpots;
    }

    @Data
    @AllArgsConstructor
    public static class PriceTierDto {
        private String label;      // e.g. "5-10 Students"
        private BigDecimal price;  // Calculated price per person
    }
}