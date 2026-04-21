package com.rental_api.ServiceBooking.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This DTO is used when a user sends a message via WebSocket or REST.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {
    
    // The ID of the student or tutor receiving the message
    private Long recipientId;
    
    // The actual text content of the message
    private String content;
}