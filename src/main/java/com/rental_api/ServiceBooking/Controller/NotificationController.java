package com.rental_api.ServiceBooking.Controller;

import com.rental_api.ServiceBooking.Entity.Notification;
import com.rental_api.ServiceBooking.Repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /* ================= GET NOTIFICATIONS ================= */
    @GetMapping("/my-notifications")
    @PreAuthorize("hasAnyRole('TUTOR','STUDENT')")
    public ResponseEntity<List<Notification>> getMyNotifications() {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        return ResponseEntity.ok(
                notificationRepository.findByRecipientEmailOrderByCreatedAtDesc(email)
        );
    }

    /* ================= UNREAD COUNT ================= */
    @GetMapping("/unread-count")
    @PreAuthorize("hasAnyRole('TUTOR','STUDENT')")
    public ResponseEntity<Long> getUnreadCount() {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        return ResponseEntity.ok(
                notificationRepository.countByRecipientEmailAndIsReadFalse(email)
        );
    }

    /* ================= MARK AS READ ================= */
    @PatchMapping("/read/{id}")
    @PreAuthorize("hasAnyRole('TUTOR','STUDENT')")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getRecipientEmail().equals(email)) {
            return ResponseEntity.status(403).build();
        }

        notification.setRead(true);
        notificationRepository.save(notification);

        return ResponseEntity.ok().build();
    }

    /* ================= MARK ALL AS READ ================= */
    @PatchMapping("/read-all")
    @PreAuthorize("hasAnyRole('TUTOR','STUDENT')")
    public ResponseEntity<Void> markAllAsRead() {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        List<Notification> list =
                notificationRepository.findByRecipientEmailOrderByCreatedAtDesc(email);

        list.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(list);

        return ResponseEntity.ok().build();
    }

    /* ================= DELETE ================= */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('TUTOR','STUDENT')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getRecipientEmail().equals(email)) {
            return ResponseEntity.status(403).build();
        }

        notificationRepository.delete(notification);

        return ResponseEntity.ok().build();
    }

    /* ================= REAL-TIME PUSH (IMPORTANT FOR CHAT FLOW) ================= */
    public void pushNotification(String email, String type, String content, Long bookingId) {

        Notification notification = Notification.builder()
                .recipientEmail(email)
                .type(type)
                .content(content)
                .bookingId(bookingId)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);

        // 🔥 WebSocket push to frontend
        messagingTemplate.convertAndSendToUser(
                email,
                "/queue/notifications",
                notification
        );
    }
}