package com.rental_api.ServiceBooking.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DayTimeSlot {

    @Enumerated(EnumType.STRING)
    private DayOfWeek day;

    private String startTime; // "08:00"
    private String endTime; // "10:00"
}