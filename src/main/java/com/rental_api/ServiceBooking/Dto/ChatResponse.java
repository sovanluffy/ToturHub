package com.rental_api.ServiceBooking.Dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChatResponse {
    private Long id;
    private Long senderId;
    private String senderName;
    private String senderAvatar;
    private Long recipientId;
    private String content;
    private LocalDateTime timestamp;
    private boolean isRead;
    private boolean isOnline; // Calculated at runtime
}