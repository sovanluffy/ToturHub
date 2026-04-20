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

    // ================= GET MY NOTIFICATIONS =================
    @GetMapping("/my-notifications")
    @PreAuthorize("hasAnyRole('TUTOR', 'STUDENT')")
    public ResponseEntity<List<Notification>> getMyNotifications() {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        List<Notification> notifications =
                notificationRepository.findByRecipientEmailOrderByCreatedAtDesc(email);

        return ResponseEntity.ok(notifications);
    }

    // ================= UNREAD COUNT =================
    @GetMapping("/unread-count")
    @PreAuthorize("hasAnyRole('TUTOR', 'STUDENT')")
    public ResponseEntity<Long> getUnreadCount() {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Long count = notificationRepository
                .countByRecipientEmailAndIsReadFalse(email);

        return ResponseEntity.ok(count);
    }

    // ================= MARK ONE AS READ =================
    @PatchMapping("/read/{id}")
    @PreAuthorize("hasAnyRole('TUTOR', 'STUDENT')")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        // 🔒 Security check (user can only update their own notification)
        if (!notification.getRecipientEmail().equals(email)) {
            return ResponseEntity.status(403).build();
        }

        notification.setRead(true);
        notificationRepository.save(notification);

        return ResponseEntity.ok().build();
    }

    // ================= MARK ALL AS READ =================
    @PatchMapping("/read-all")
    @PreAuthorize("hasAnyRole('TUTOR', 'STUDENT')")
    public ResponseEntity<Void> markAllAsRead() {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        List<Notification> notifications =
                notificationRepository.findByRecipientEmailOrderByCreatedAtDesc(email);

        notifications.forEach(n -> n.setRead(true));

        notificationRepository.saveAll(notifications);

        return ResponseEntity.ok().build();
    }

    // ================= DELETE NOTIFICATION =================
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('TUTOR', 'STUDENT')")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getRecipientEmail().equals(email)) {
            return ResponseEntity.status(403).build();
        }

        notificationRepository.delete(notification);

        return ResponseEntity.ok().build();
    }
}