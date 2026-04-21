package com.rental_api.ServiceBooking.Controller;

import com.rental_api.ServiceBooking.Dto.ChatRequest;
import com.rental_api.ServiceBooking.Dto.ChatResponse;
import com.rental_api.ServiceBooking.Services.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final BookingService bookingService;

    // ================= SEND MESSAGE =================
    @PostMapping("/send")
    public ResponseEntity<ChatResponse> sendMessage(
            Authentication authentication,
            @RequestBody ChatRequest request
    ) {
        String senderEmail = authentication.getName();
        return ResponseEntity.ok(
                bookingService.sendMessage(senderEmail, request)
        );
    }

    // ================= CHAT HISTORY =================
    @GetMapping("/history/{otherUserId}")
    public ResponseEntity<List<ChatResponse>> getHistory(
            Authentication authentication,
            @PathVariable Long otherUserId
    ) {
        String myEmail = authentication.getName();
        return ResponseEntity.ok(
                bookingService.getChatHistory(myEmail, otherUserId)
        );
    }

    // ================= MARK AS READ (FIXED) =================
    @PutMapping("/read/{senderId}")
    public ResponseEntity<Void> markAsRead(
            Authentication authentication,
            @PathVariable Long senderId
    ) {
        String recipientEmail = authentication.getName();

        bookingService.markMessagesAsRead(recipientEmail, senderId);

        return ResponseEntity.ok().build();
    }
}