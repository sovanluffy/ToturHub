package com.rental_api.ServiceBooking.Controller;

import com.rental_api.ServiceBooking.Dto.ChatRequest;
import com.rental_api.ServiceBooking.Dto.ChatResponse;
import com.rental_api.ServiceBooking.Services.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final BookingService bookingService;

    /* ================= SEND MESSAGE ================= */
    @PostMapping("/send")
    public ChatResponse sendMessage(
            Principal principal,
            @RequestBody ChatRequest request
    ) {
        return bookingService.sendMessage(principal.getName(), request);
    }

    /* ================= CHAT HISTORY ================= */
    @GetMapping("/history/{otherUserId}")
    public List<ChatResponse> getChatHistory(
            Principal principal,
            @PathVariable Long otherUserId
    ) {
        return bookingService.getChatHistory(
                principal.getName(),
                otherUserId
        );
    }

    /* ================= MARK AS READ ================= */
    @PutMapping("/read/{senderId}")
    public void markAsRead(
            Principal principal,
            @PathVariable Long senderId
    ) {
        bookingService.markMessagesAsRead(
                principal.getName(),
                senderId
        );
    }
}