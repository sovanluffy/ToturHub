package com.rental_api.ServiceBooking.Dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatContactResponse {
    private Long userId;
    private String name;
    private String avatar;
    private String lastMessage;
    private Long unreadCount;
    private String lastTime;
}