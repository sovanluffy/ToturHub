package com.rental_api.ServiceBooking.Dto.Response;

import java.time.LocalDate;
import java.time.LocalTime;

import com.rental_api.ServiceBooking.Entity.Enum.BookingStatus;

import lombok.Data;

@Data
public class BookingResponse {
        private Long id;
        private Long scheduleId;
        private String scheduleType; 
        private LocalDate startDate;
        private LocalDate endDate;
        private LocalTime startTime;
        private LocalTime endTime;
        private BookingStatus status;
}
