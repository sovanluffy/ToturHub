package com.rental_api.ServiceBooking.Services;

import org.springframework.stereotype.Service;

@Service
public interface NotificationService {
     void sendNotification(String email, String content);
}
