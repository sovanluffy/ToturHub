package com.rental_api.ServiceBooking.Dto.Request;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
public class OpenClassRequest {
    private String title;
    private String description;
    private List<Long> subjectIds;
    private List<String> learningModes;
    
    private Long locationId; 
    private String specificAddress;

    private Map<Integer, BigDecimal> priceOptions;

    // --- RECURRING SCHEDULE LOGIC ---
    // Change from single fields to a List to support "Daily AND Weekend"
    private List<ScheduleConfig> schedules;

    @Data
    public static class ScheduleConfig {
        private String scheduleType; // "DAILY", "WEEKEND", "WEEKDAY"
        private LocalDate startDate;
        private LocalDate endDate;
        private List<TimeRangeRequest> timeRanges; 
    }

    @Data
    public static class TimeRangeRequest {
        private String startTime; // "08:00"
        private String endTime;   // "10:00"
    }
}