package com.rental_api.ServiceBooking.Controller;

import com.rental_api.ServiceBooking.Dto.ChatRequest;
import com.rental_api.ServiceBooking.Dto.ChatResponse;
import com.rental_api.ServiceBooking.Services.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
@Tag(name = "Chat Management", description = "Endpoints for real-time messaging between Tutors and Students")
public class ChatController {

    private final BookingService bookingService;

    // ================= GET CHAT HISTORY =================
    @Operation(summary = "Get chat history with a specific user")
    @GetMapping("/history/{otherUserId}")
    public ResponseEntity<List<ChatResponse>> getChatHistory(
            @PathVariable Long otherUserId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                bookingService.getChatHistory(authentication.getName(), otherUserId)
        );
    }

    // ================= MARK AS SEEN =================
    @Operation(summary = "Mark all unread messages from a sender as seen")
    @PatchMapping("/seen/{senderId}")
    public ResponseEntity<Void> markAsSeen(
            @PathVariable Long senderId,
            Authentication authentication
    ) {
        bookingService.markMessagesAsRead(authentication.getName(), senderId);
        return ResponseEntity.ok().build();
    }

    // ================= WEBSOCKET: SEND MESSAGE =================
    /**
     * Note: This is a WebSocket endpoint, not a standard REST endpoint.
     * It will NOT show up in Swagger UI as an executable HTTP request, 
     * but you can document it in your API docs.
     */
    @MessageMapping("/chat.send")
    public void processMessage(@Payload ChatRequest chatRequest, Principal principal) {
        bookingService.sendMessage(principal.getName(), chatRequest);
    }
}