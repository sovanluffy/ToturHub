package com.rental_api.ServiceBooking.Dto.Response;

import lombok.*;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.*;

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

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DayTimeSlotResponse {
        private DayOfWeek day;
        private String startTime;
        private String endTime;
    }
}