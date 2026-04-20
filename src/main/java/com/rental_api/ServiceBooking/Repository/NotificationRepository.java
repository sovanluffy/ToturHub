package com.rental_api.ServiceBooking.Repository;

import com.rental_api.ServiceBooking.Entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // Find all notifications for a specific user, newest first
    List<Notification> findByRecipientEmailOrderByCreatedAtDesc(String email);
    
    // Count unread notifications
    long countByRecipientEmailAndIsReadFalse(String email);
}