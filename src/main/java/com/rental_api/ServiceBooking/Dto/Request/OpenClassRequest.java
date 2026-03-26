package com.rental_api.ServiceBooking.Dto.Request;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
public class OpenClassRequest {
    private String title;
    private String description;
    private Long tutorId;
    private List<Long> subjectIds;
    private Map<Integer, BigDecimal> priceOptions; // "Put more" prices
    private Set<String> learningModes; // "Choose more" modes (Home, Online, etc.)
    private String city;
    private String district;
    private String address;
    private List<ScheduleRequest> timeSlots;

    @Data
    public static class ScheduleRequest {
        private LocalDateTime startTime;
        private LocalDateTime endTime;
    }
}