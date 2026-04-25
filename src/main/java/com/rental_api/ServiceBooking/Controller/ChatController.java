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
    // UTIL: GET CURRENT AUTHENTICATED EMAIL
    // =====================================================
    private String getCurrentEmail() {
        return SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
    }

    /**
     * SEND MESSAGE (TEXT + FILE + CLOUDINARY)
     * Consumes multipart/form-data to allow file uploads.
     */
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

        // Wrap parameters into a Request DTO for the service layer
        ChatRequest request = new ChatRequest();
        request.setRecipientId(recipientId);
        request.setContent(content);
        request.setBookingId(bookingId);
        request.setFile(file);

        return ResponseEntity.ok(
                bookingService.sendMessage(getCurrentEmail(), request)
        );
    }

    /**
     * CHAT HISTORY
     * Retrieves all messages exchanged between the current user and another user.
     */
    @GetMapping("/history/{otherUserId}")
    @PreAuthorize("hasAnyRole('STUDENT','TUTOR')")
    public ResponseEntity<List<ChatResponse>> getChatHistory(
            @PathVariable Long otherUserId
    ) {
        return ResponseEntity.ok(
                bookingService.getChatHistory(getCurrentEmail(), otherUserId)
        );
    }

    /**
     * MARK AS READ
     * Updates the 'read' status of all unread messages from a specific sender.
     */
    @PutMapping("/read/{senderId}")
    @PreAuthorize("hasAnyRole('STUDENT','TUTOR')")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long senderId
    ) {
        bookingService.markMessagesAsRead(getCurrentEmail(), senderId);
        return ResponseEntity.ok().build();
    }

    /**
     * UNREAD COUNT
     * Gets total count of unread messages for the current user across all conversations.
     */
    @GetMapping("/unread-count")
    @PreAuthorize("hasAnyRole('STUDENT','TUTOR')")
    public ResponseEntity<Long> getUnreadCount() {
        return ResponseEntity.ok(
                bookingService.getUnreadMessageCount(getCurrentEmail())
        );
    }

    /**
     * CHAT CONTACTS
     * Returns a list of users the current user has chatted with, including last message and unread count.
     */
    @GetMapping("/contacts")
    @PreAuthorize("hasAnyRole('STUDENT','TUTOR')")
    public ResponseEntity<List<ChatContactResponse>> getChatContacts() {
        return ResponseEntity.ok(
                bookingService.getChatContacts(getCurrentEmail())
        );
    }

    /**
     * CHAT USERS (DEBUG/LIST)
     * Simple list of User IDs involved in conversations with the current user.
     */
    @GetMapping("/users")
    @PreAuthorize("hasAnyRole('STUDENT','TUTOR')")
    public ResponseEntity<List<Long>> getChatUserList() {
        return ResponseEntity.ok(
                bookingService.getChatUserList(getCurrentEmail())
        );
    }
}