package com.rental_api.ServiceBooking.Dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {

    private Long id;
    private Long senderId;
    private Long recipientId;
    private String content;
    private LocalDateTime timestamp;
    private boolean read;

    private String messageType;

    private Long bookingId;
    private String bookingStatus;

    private boolean online;

    private String senderName;
    private String senderAvatar;
}