package com.rental_api.ServiceBooking.Dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatResponse {

    // ================= BASIC MESSAGE =================
    private Long id;
    private Long senderId;
    private Long recipientId;
    private String content;
    private LocalDateTime timestamp;
    private boolean read;

    // ================= MESSAGE TYPE =================
    // USER | SYSTEM | BOOKING
    private String messageType;

    // ================= BOOKING SUPPORT =================
    private Long bookingId;
    private String bookingStatus; // CONFIRMED | REJECTED | PENDING

    // ================= UI SUPPORT =================
    private boolean online;

    // optional (future upgrade)
    private String senderName;
    private String senderAvatar;
}