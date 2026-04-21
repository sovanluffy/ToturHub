package com.rental_api.ServiceBooking.Dto;

import lombok.Data;

@Data
public class ChatRequest {

    private Long recipientId;
    private String content;

    // optional (used for booking/chat integration later)
    private Long bookingId;
}