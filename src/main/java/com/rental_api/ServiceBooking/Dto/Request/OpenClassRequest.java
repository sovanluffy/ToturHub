package com.rental_api.ServiceBooking.Dto.Request;

import com.rental_api.ServiceBooking.Entity.Enum.DurationType;
import lombok.*;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpenClassRequest {

    // ================= BASIC INFO =================
    private String title;
    private String description;

    private List<Long> subjectIds;

    private String status;

    // ================= LOCATION =================
    private Long locationId;
    private String specificAddress;

    // ================= PRICE =================
    private BigDecimal basePrice;

    private Integer maxStudents; // CLASS LEVEL LIMIT

    // ================= LEARNING =================
    private List<String> learningModes;

    // ================= SCHEDULE =================
    private List<DayTimeSlotRequest> dayTimeSlots;

    // ================= CLASS DURATION (🔥 NEW ADDED) =================
    private LocalDateTime startDate;

    private DurationType durationType;

    private Integer durationValue;

    // ================= SLOT DTO =================
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DayTimeSlotRequest {

        private DayOfWeek day;
        private LocalTime startTime;
        private LocalTime endTime;

        // SLOT LEVEL LIMIT (override class max if needed)
        private Integer maxStudents;
    }
}