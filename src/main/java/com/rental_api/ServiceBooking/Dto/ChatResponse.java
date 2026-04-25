package com.rental_api.ServiceBooking.Dto;

import lombok.*;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {

    // ================= BASIC =================
    private Long id;

    private Long senderId;
    private Long recipientId;

    // ================= TEXT =================
    private String content;

    // ================= MEDIA =================
    private String mediaUrl;   // Cloudinary URL
    private String fileType;   // IMAGE / VIDEO / AUDIO

    // ================= TIME =================
    private Instant timestamp;
    private boolean read;

    // ================= TYPE =================
    private String messageType;
    // TEXT / IMAGE / VIDEO / AUDIO / SYSTEM / BOOKING

    // ================= BOOKING =================
    private Long bookingId;
    private String bookingStatus;

    // ================= USER INFO =================
    private String senderName;
    private String senderAvatar;
    private boolean senderOnline;

    // =====================================================
    // ================= FRONTEND HELPERS ==================
    // =====================================================

    /**
     * Check if message contains media
     */
    public boolean isMediaMessage() {
        return mediaUrl != null && !mediaUrl.trim().isEmpty();
    }

    /**
     * Check if message is text-only
     */
    public boolean isTextMessage() {
        return content != null && !content.trim().isEmpty();
    }

    /**
     * Quick check for system messages
     */
    public boolean isSystemMessage() {
        return "SYSTEM".equalsIgnoreCase(messageType);
    }

    /**
     * Quick check for booking messages
     */
    public boolean isBookingMessage() {
        return bookingId != null;
    }
}