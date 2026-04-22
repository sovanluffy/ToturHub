package com.rental_api.ServiceBooking.Controller;

import com.rental_api.ServiceBooking.Dto.ChatContactResponse;
import com.rental_api.ServiceBooking.Dto.ChatRequest;
import com.rental_api.ServiceBooking.Dto.ChatResponse;
import com.rental_api.ServiceBooking.Services.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final BookingService bookingService;

    // ================= SEND MESSAGE =================
    @PostMapping("/send")
    @PreAuthorize("hasAnyRole('STUDENT','TUTOR')")
    public ResponseEntity<ChatResponse> sendMessage(
            @RequestBody ChatRequest request
    ) {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return ResponseEntity.ok(
                bookingService.sendMessage(email, request)
        );
    }

    // ================= CHAT HISTORY =================
    @GetMapping("/history/{otherUserId}")
    @PreAuthorize("hasAnyRole('STUDENT','TUTOR')")
    public ResponseEntity<List<ChatResponse>> getChatHistory(
            @PathVariable Long otherUserId
    ) {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return ResponseEntity.ok(
                bookingService.getChatHistory(email, otherUserId)
        );
    }

    // ================= MARK AS READ =================
    @PutMapping("/read/{senderId}")
    @PreAuthorize("hasAnyRole('STUDENT','TUTOR')")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long senderId
    ) {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        bookingService.markMessagesAsRead(email, senderId);

        return ResponseEntity.ok().build();
    }

    // ================= UNREAD COUNT =================
    @GetMapping("/unread-count")
    @PreAuthorize("hasAnyRole('STUDENT','TUTOR')")
    public ResponseEntity<Long> getUnreadCount() {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return ResponseEntity.ok(
                bookingService.getUnreadMessageCount(email)
        );
    }

    // ================= ⭐ CHAT CONTACT LIST (IMPORTANT FIX) =================
    @GetMapping("/contacts")
    @PreAuthorize("hasAnyRole('STUDENT','TUTOR')")
    public ResponseEntity<List<ChatContactResponse>> getChatContacts() {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return ResponseEntity.ok(
                bookingService.getChatContacts(email)
        );
    }

    // ================= CHAT USER LIST (OPTIONAL DEBUG) =================
    @GetMapping("/users")
    @PreAuthorize("hasAnyRole('STUDENT','TUTOR')")
    public ResponseEntity<List<Long>> getChatUserList() {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return ResponseEntity.ok(
                bookingService.getChatUserList(email)
        );
    }
}