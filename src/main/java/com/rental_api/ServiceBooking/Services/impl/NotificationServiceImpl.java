package com.rental_api.ServiceBooking.Services.impl;

import java.time.LocalDateTime;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.rental_api.ServiceBooking.Entity.Notification;
import com.rental_api.ServiceBooking.Services.NotificationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void sendNotification(String recipient, String message) {
        // Create the notification object
        Notification notification = new Notification();
        notification.setContent(message);
        notification.setTimestamp(LocalDateTime.now().toString());

        // Use convertAndSendToUser to target the specific tutor/recipient
        // Frontend must subscribe to /user/queue/notifications
        messagingTemplate.convertAndSendToUser(
            recipient, 
            "/queue/notifications", 
            notification
        );
    }
}
