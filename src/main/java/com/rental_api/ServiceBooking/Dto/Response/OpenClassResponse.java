package com.rental_api.ServiceBooking.Dto.Response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
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
    private String location; 
    private String specificAddress;

    // Data Lists
    private List<String> subjects;
    private Set<String> learningModes;
    private BigDecimal basePrice;
    private Integer maxStudents;
    private Integer currentStudents;
    private List<PriceTierDto> priceOptions;

    // ✅ Change availableSlots to schedules to match Frontend expectation
    private List<ScheduleConfigResponse> schedules; 

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScheduleConfigResponse {
        private Long id; // The ScheduleConfig ID needed for booking
        private String scheduleType; // DAILY, WEEKEND, etc.
        private LocalDate startDate;
        private LocalDate endDate;
        private String startTime; 
        private String endTime;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PriceTierDto {
        private String label;      
        private BigDecimal price;  
    }
}