package com.rental_api.ServiceBooking.Controller;

import com.rental_api.ServiceBooking.Dto.ChatContactResponse;
import com.rental_api.ServiceBooking.Dto.ChatRequest;
import com.rental_api.ServiceBooking.Dto.ChatResponse;
import com.rental_api.ServiceBooking.Services.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final BookingService bookingService;

    // =====================================================
    // CURRENT USER
    // =====================================================
    private String getCurrentEmail() {
        return SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
    }

    // =====================================================
    // SEND MESSAGE (TEXT + FILE + CLOUDINARY)
    // =====================================================
    @PostMapping(
            value = "/send",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasAnyRole('STUDENT','TUTOR')")
    public ResponseEntity<ChatResponse> sendMessage(
            @RequestParam("recipientId") Long recipientId,
            @RequestParam(value = "content", required = false) String content,
            @RequestParam(value = "bookingId", required = false) Long bookingId,
            @RequestParam(value = "file", required = false) MultipartFile file
    ) {

        ChatRequest request = new ChatRequest();
        request.setRecipientId(recipientId);
        request.setContent(content);
        request.setBookingId(bookingId);
        request.setFile(file);

        return ResponseEntity.ok(
                bookingService.sendMessage(getCurrentEmail(), request)
        );
    }

    // =====================================================
    // CHAT HISTORY
    // =====================================================
    @GetMapping("/history/{otherUserId}")
    @PreAuthorize("hasAnyRole('STUDENT','TUTOR')")
    public ResponseEntity<List<ChatResponse>> getChatHistory(
            @PathVariable Long otherUserId
    ) {
        return ResponseEntity.ok(
                bookingService.getChatHistory(getCurrentEmail(), otherUserId)
        );
    }

    // =====================================================
    // MARK AS READ
    // =====================================================
    @PutMapping("/read/{senderId}")
    @PreAuthorize("hasAnyRole('STUDENT','TUTOR')")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long senderId
    ) {
        bookingService.markMessagesAsRead(getCurrentEmail(), senderId);
        return ResponseEntity.ok().build();
    }

    // =====================================================
    // UNREAD COUNT
    // =====================================================
    @GetMapping("/unread-count")
    @PreAuthorize("hasAnyRole('STUDENT','TUTOR')")
    public ResponseEntity<Long> getUnreadCount() {
        return ResponseEntity.ok(
                bookingService.getUnreadMessageCount(getCurrentEmail())
        );
    }

    // =====================================================
    // CHAT CONTACTS
    // =====================================================
    @GetMapping("/contacts")
    @PreAuthorize("hasAnyRole('STUDENT','TUTOR')")
    public ResponseEntity<List<ChatContactResponse>> getChatContacts() {
        return ResponseEntity.ok(
                bookingService.getChatContacts(getCurrentEmail())
        );
    }

    // =====================================================
    // CHAT USERS (DEBUG)
    // =====================================================
    @GetMapping("/users")
    @PreAuthorize("hasAnyRole('STUDENT','TUTOR')")
    public ResponseEntity<List<Long>> getChatUserList() {
        return ResponseEntity.ok(
                bookingService.getChatUserList(getCurrentEmail())
        );
    }
}