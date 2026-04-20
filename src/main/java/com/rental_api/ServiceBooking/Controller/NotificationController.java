package com.rental_api.ServiceBooking.Controller;

import com.rental_api.ServiceBooking.Entity.Notification;
import com.rental_api.ServiceBooking.Repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;

    // GET all notifications for the logged-in Tutor/User
    @GetMapping("/my-notifications")
    @PreAuthorize("hasAnyRole('TUTOR', 'STUDENT')")
    public ResponseEntity<List<Notification>> getMyNotifications() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(notificationRepository.findByRecipientEmailOrderByCreatedAtDesc(email));
    }

    // GET unread count
    @GetMapping("/unread-count")
    @PreAuthorize("hasAnyRole('TUTOR', 'STUDENT')")
    public ResponseEntity<Long> getUnreadCount() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(notificationRepository.countByRecipientEmailAndIsReadFalse(email));
    }

    // Mark a specific notification as read
    @PatchMapping("/read/{id}")
    @PreAuthorize("hasAnyRole('TUTOR', 'STUDENT')")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationRepository.findById(id).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
        return ResponseEntity.ok().build();
    }

    // Mark ALL as read
    @PatchMapping("/read-all")
    @PreAuthorize("hasAnyRole('TUTOR', 'STUDENT')")
    public ResponseEntity<Void> markAllAsRead() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Notification> unread = notificationRepository.findByRecipientEmailOrderByCreatedAtDesc(email);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
        return ResponseEntity.ok().build();
    }
}