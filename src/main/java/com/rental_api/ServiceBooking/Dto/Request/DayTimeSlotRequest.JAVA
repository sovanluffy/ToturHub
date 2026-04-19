package com.rental_api.ServiceBooking.Dto.Request;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DayTimeSlotRequest {

    private String day;       // "MONDAY"
    private String startTime; // "08:00"
    private String endTime;   // "10:00"
}