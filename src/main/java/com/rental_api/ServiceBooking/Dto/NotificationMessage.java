package com.rental_api.ServiceBooking.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NotificationMessage {
    private String type; // BOOKING_REQUEST, BOOKING_CONFIRMED, etc.
    private String message;
    private Long bookingId;
    private Long classId;
}