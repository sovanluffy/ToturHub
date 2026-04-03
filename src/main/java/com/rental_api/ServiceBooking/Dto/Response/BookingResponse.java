package com.rental_api.ServiceBooking.Dto.Response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.rental_api.ServiceBooking.Entity.Enum.BookingStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingResponse {
    private Long bookingId;
    private Long scheduleId;
    private String scheduleType; 
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private BookingStatus status;
    private String note;
    private LocalDateTime createdAt; // Must be LocalDateTime to match Entity
}