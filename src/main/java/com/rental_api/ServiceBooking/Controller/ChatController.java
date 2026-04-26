package com.rental_api.ServiceBooking.Controller;

import com.rental_api.ServiceBooking.Dto.ChatContactResponse;
import com.rental_api.ServiceBooking.Dto.ChatRequest;
import com.rental_api.ServiceBooking.Dto.ChatResponse;
import com.rental_api.ServiceBooking.Services.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Controller for handling all Chat-related operations.
 * Integrated with WebSocket logic via BookingService.
 */
@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final BookingService bookingService;

    /**
     * Helper to get the email of the currently logged-in user from Security Context.
     */
    private String getCurrentEmail() {
        return SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
    }

    // =====================================================
    // SEND MESSAGE (TEXT + FILE)
    // =====================================================
    
    /**
     * Send a message to another user. 
     * If a file is provided, it is uploaded to Cloudinary automatically.
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
        // Prepare DTO for service layer
        ChatRequest request = new ChatRequest();
        request.setRecipientId(recipientId);
        request.setContent(content);
        request.setBookingId(bookingId);
        request.setFile(file);

        // Service layer handles DB saving and WebSocket push
        ChatResponse response = bookingService.sendMessage(getCurrentEmail(), request);
        
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // =====================================================
    // RETRIEVAL LOGIC
    // =====================================================

    /**
     * Gets all chat messages between the current user and another user.
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
     * Returns a summarized list of all people the user has chatted with.
     * Includes: last message, unread count, and last timestamp.
     */
    @GetMapping("/contacts")
    @PreAuthorize("hasAnyRole('STUDENT','TUTOR')")
    public ResponseEntity<List<ChatContactResponse>> getChatContacts() {
        return ResponseEntity.ok(
                bookingService.getChatContacts(getCurrentEmail())
        );
    }

    // =====================================================
    // STATUS & NOTIFICATION LOGIC
    // =====================================================

    /**
     * Mark all unread messages from a specific sender as 'read'.
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
     * Quick check for total unread messages for the logged-in user.
     */
    @GetMapping("/unread-count")
    @PreAuthorize("hasAnyRole('STUDENT','TUTOR')")
    public ResponseEntity<Long> getUnreadCount() {
        return ResponseEntity.ok(
                bookingService.getUnreadMessageCount(getCurrentEmail())
        );
    }

    /**
     * Utility to get a simple list of User IDs the current user is interacting with.
     */
    @GetMapping("/users")
    @PreAuthorize("hasAnyRole('STUDENT','TUTOR')")
    public ResponseEntity<List<Long>> getChatUserList() {
        return ResponseEntity.ok(
                bookingService.getChatUserList(getCurrentEmail())
        );
    }
}