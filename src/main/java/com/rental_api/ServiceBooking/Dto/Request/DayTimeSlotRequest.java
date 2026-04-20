package com.rental_api.ServiceBooking.Dto.Request;

import com.rental_api.ServiceBooking.Entity.DayTimeSlot;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DayTimeSlotRequest {

    private DayOfWeek day;   // MONDAY, TUESDAY, etc.

    // Change startTime and endTime to LocalTime instead of String
    private LocalTime startTime; // "09:00"
    private LocalTime endTime;   // "17:00"
     private Integer maxStudents; 
}