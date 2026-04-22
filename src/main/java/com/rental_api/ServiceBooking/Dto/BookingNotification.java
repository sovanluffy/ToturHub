package com.rental_api.ServiceBooking.Dto;

import lombok.Data;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@Builder
public class BookingNotification {

    private String type; // BOOKING_UPDATE

    private Long bookingId;
    private String status;

    private String message;

    private Long tutorId;
    private Long studentId;

    private LocalDateTime timestamp;
}