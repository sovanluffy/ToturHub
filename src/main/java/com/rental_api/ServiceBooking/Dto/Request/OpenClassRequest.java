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

    private String title;          // Title of the class
    private String description;    // Description of the class

    private List<Long> subjectIds; // List of subject IDs related to the class

    private String status;         // Status of the class (e.g., Active, Inactive)

    private Long locationId;       // Location ID of the class
    private String specificAddress; // Specific address of the class

    private BigDecimal basePrice;  // Base price for the class
    private Integer maxStudents;   // Maximum number of students allowed

    private List<String> learningModes; // List of learning modes (e.g., Online, Offline)

    private List<DayTimeSlotRequest> dayTimeSlots; // List of day/time slots for the class

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DayTimeSlotRequest {

        private DayOfWeek day;       // Day of the week (e.g., MONDAY, TUESDAY)

        // Time slots represented by LocalTime
        private LocalTime startTime; // Start time (e.g., "09:00")
        private LocalTime endTime;   // End time (e.g., "17:00")
    }
}