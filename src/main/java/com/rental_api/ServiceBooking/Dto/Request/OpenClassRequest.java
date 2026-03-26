package com.rental_api.ServiceBooking.Dto.Request;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class OpenClassRequest {
    private String title;
    private String description;
    private List<Long> subjectIds;
    private List<String> learningModes;
    private String city;
    private String district;
    private String address;
    private Map<Integer, BigDecimal> priceOptions;
    private List<TimeSlotRequest> timeSlots;

    @Data
    public static class TimeSlotRequest {
        private LocalDateTime startTime;
        private LocalDateTime endTime;
    }
}