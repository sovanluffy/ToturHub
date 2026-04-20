package com.rental_api.ServiceBooking.Controller;

import java.security.Principal;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rental_api.ServiceBooking.Entity.Notification;
import com.rental_api.ServiceBooking.Repository.NotificationRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {


    private final NotificationRepository repository;

    // Get all unread notifications for the bell badge
    @GetMapping("/unread-count")
    public Long getUnreadCount(Principal principal) {
        return repository.countByRecipientEmailAndIsReadFalse(principal.getName());
    }

    // Mark all as read when user clicks the bell
    @PostMapping("/mark-as-read")
    public void markAsRead(Principal principal) {
        List<Notification> unread = repository.findAllByRecipientEmailAndIsReadFalse(principal.getName());
        unread.forEach(n -> n.setRead(true));
        repository.saveAll(unread);
    }
}
