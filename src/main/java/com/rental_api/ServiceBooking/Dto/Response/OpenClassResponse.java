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
    
    // --- ADD THESE FIELDS TO FIX THE ERROR ---
    private String tutorName;
    private String tutorImage;
    private Double tutorRating;
    // -----------------------------------------

    private Map<Integer, BigDecimal> pricing;
    private Set<String> modes;
    private String status;
    private String location;

    // For the "See Details" view
    private List<ScheduleDto> availableSlots;

    @Data
    @Builder
    public static class ScheduleDto {
        private Long id;
        private String timeRange;
    }
}