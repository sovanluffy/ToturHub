package com.rental_api.ServiceBooking.Dto.Response;

import lombok.*;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpenClassResponse {

    private Long classId;
    private String title;
    private String description;

    private String status;

    private Long tutorId;
    private String tutorName;
    private Double tutorRating;

    private String location;
    private String specificAddress;

    private List<String> subjects;
    private Set<String> learningModes;

    private BigDecimal basePrice;
    private Integer maxStudents;
    private Integer currentStudents;

    private String classImage;

    private List<DayTimeSlotResponse> schedules;

    // ================= INNER CLASS =================
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DayTimeSlotResponse {

        private Long id;

        private DayOfWeek day;
        private LocalTime startTime;
        private LocalTime endTime;

        // 🔥 ADD THESE FOR BOOKING SYSTEM
        private Integer maxStudents;
        private Integer bookedCount;
    }
}