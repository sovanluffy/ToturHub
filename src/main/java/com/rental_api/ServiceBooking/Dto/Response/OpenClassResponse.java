package com.rental_api.ServiceBooking.Dto.Response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class OpenClassResponse {
    private Long classId;
    private String title;
    private String tutorName;
    private List<String> subjects;
    private Map<Integer, BigDecimal> pricing;
    private String learningMode;
    private String location;
    private List<ScheduleDto> availableSlots;

    @Data
    @Builder
    public static class ScheduleDto {
        private Long id; // Student clicks this ID to book
        private String timeRange;
        private boolean isBooked;
    }
}