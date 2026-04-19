package com.rental_api.ServiceBooking.Dto.Request;

import com.rental_api.ServiceBooking.Entity.DayTimeSlot;
import lombok.*;

import java.time.DayOfWeek;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DayTimeSlotRequest {

    private DayOfWeek day;   // MONDAY, TUESDAY, etc.

    private String startTime; // "09:00"
    private String endTime;   // "17:00"
}