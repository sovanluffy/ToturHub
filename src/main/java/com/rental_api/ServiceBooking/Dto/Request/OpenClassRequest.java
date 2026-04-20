package com.rental_api.ServiceBooking.Dto.Request;

import lombok.*;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpenClassRequest {

    private String title;
    private String description;

    private List<Long> subjectIds;

    private String status;

    private Long locationId;
    private String specificAddress;

    private BigDecimal basePrice;

    private Integer maxStudents; // 🔥 CLASS LEVEL LIMIT

    private List<String> learningModes;

    private List<DayTimeSlotRequest> dayTimeSlots;

    // ================= SLOT DTO =================
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DayTimeSlotRequest {

        private DayOfWeek day;
        private LocalTime startTime;
        private LocalTime endTime;

        // 🔥 ADD THIS (IMPORTANT)
        private Integer maxStudents; // SLOT LEVEL LIMIT
    }
}