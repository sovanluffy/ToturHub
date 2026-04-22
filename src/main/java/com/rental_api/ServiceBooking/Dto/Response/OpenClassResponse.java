package com.rental_api.ServiceBooking.Dto.Response;

import lombok.*;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
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

    // ================= TUTOR =================
    private TutorPublicResponse tutor;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TutorPublicResponse {
        private Long tutorId;
        private String name;
        private String avatar;
        private Double rating;
        private String email;
        private String phone;
    }

    // ================= LOCATION =================
    private String location;
    private String specificAddress;

    // ================= CLASS INFO =================
    private List<String> subjects;
    private Set<String> learningModes;

    private BigDecimal basePrice;
    private Integer maxStudents;
    private Integer currentStudents;

    private String classImage;

    // ================= 24H NEW SYSTEM =================
    private boolean isNew;
    private LocalDateTime createdAt;
    private LocalDateTime newUntil;

    // ================= CONFIRMED STUDENTS =================
    private List<StudentPublicResponse> confirmedStudents;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentPublicResponse {
        private Long studentId;
        private String studentName;
        private String avatar;
        private String email;
    }

    // ================= SCHEDULE =================
    private List<DayTimeSlotResponse> schedules;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DayTimeSlotResponse {
        private Long id;
        private DayOfWeek day;
        private LocalTime startTime;
        private LocalTime endTime;
        private Integer maxStudents;
        private Integer bookedCount;
    }
}