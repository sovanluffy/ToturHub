package com.rental_api.ServiceBooking.Dto.Response;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpenClassResponse {
    private Long classId;
    private String title;
    private String description;
    private Long tutorId;
    private String tutorName;
    private String tutorImage;
    private Double tutorRating;
    private Integer yearsOfExperience;
    private List<String> subjects;
    private Map<Integer, BigDecimal> pricing;
    
    // ✅ Change this field name to 'learningModes'
    private Set<String> learningModes; 
    
    private String status;
    private String location;
    private List<ScheduleDto> availableSlots;

    @Data
    @Builder
    public static class ScheduleDto {
        private Long id;
        private String timeRange;
    }
}