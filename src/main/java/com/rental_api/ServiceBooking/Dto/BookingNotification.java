package com.rental_api.ServiceBooking.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class BookingNotification {
    private String message;
    private String senderName;
    private String timestamp;
}