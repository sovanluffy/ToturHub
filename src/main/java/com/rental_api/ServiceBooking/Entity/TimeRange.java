package com.rental_api.ServiceBooking.Entity;

import java.time.LocalTime;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeRange {
    private LocalTime startTime;
    private LocalTime endTime;
}