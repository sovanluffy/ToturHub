package com.rental_api.ServiceBooking.Services.impl;

import java.time.LocalDateTime;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.rental_api.ServiceBooking.Entity.Notification;
import com.rental_api.ServiceBooking.Repository.NotificationRepository;
import com.rental_api.ServiceBooking.Services.NotificationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationRepository repository;

    @Override
    public void sendNotification(String email, String content) {
        // Create the notification object
        Notification notif = new Notification();
        notif.setRecipientEmail(email);
        notif.setContent(content);
        notif.setTimestamp(LocalDateTime.now());
        repository.save(notif);

        // Use convertAndSendToUser to target the specific tutor/recipient
        // Frontend must subscribe to /user/queue/notifications
        messagingTemplate.convertAndSendToUser(
            email, 
            "/queue/notifications", 
            notif
        );
    }
}
