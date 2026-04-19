package com.rental_api.ServiceBooking.Dto.Request;

import lombok.*;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpenClassRequest {

    private String title;
    private String description;

    private List<Long> subjectIds;

    // ================= CLASS TYPE =================
    private String classType; // ONLINE / STUDENT_HOME / TUTOR_CLASS

    // ================= STATUS =================
    private String status; // OPEN / CLOSED / FULL

    private Long locationId;
    private String specificAddress;

    private BigDecimal basePrice;
    private Integer maxStudents;

    // ================= LEARNING MODE =================
    private List<String> learningModes;

    // ================= SCHEDULE =================
    private List<DayTimeSlotRequest> dayTimeSlots;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DayTimeSlotRequest {
        private DayOfWeek day;
        private String startTime;
        private String endTime;
    }
}