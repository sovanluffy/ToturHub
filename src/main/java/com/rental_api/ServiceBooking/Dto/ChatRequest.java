package com.rental_api.ServiceBooking.Dto;

import com.rental_api.ServiceBooking.Entity.Enum.MessageType;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ChatRequest {

    // =====================================================
    // ================= USER ==============================
    // =====================================================
    private Long recipientId;

    // =====================================================
    // ================= TEXT ==============================
    // =====================================================
    private String content;

    // =====================================================
    // ================= FILE ==============================
    // =====================================================
    private MultipartFile file; // image / video / audio / file (multipart/form-data only)

    // =====================================================
    // ================= BOOKING ===========================
    // =====================================================
    private Long bookingId;

    // ⚠️ DO NOT trust frontend type (security risk)
    // private MessageType type; ❌ REMOVE THIS

    // =====================================================
    // ================= HELPERS ===========================
    // =====================================================

    /**
     * Check if file exists
     */
    public boolean hasFile() {
        return file != null && !file.isEmpty();
    }

    /**
     * Check if text exists
     */
    public boolean hasText() {
        return content != null && !content.trim().isEmpty();
    }

    /**
     * Check booking
     */
    public boolean hasBooking() {
        return bookingId != null;
    }

    /**
     * Resolve message type safely (BACKEND CONTROLLED)
     */
    public MessageType resolveType() {

        if (hasFile()) {

            String mimeType = file.getContentType();

            if (mimeType != null) {

                if (mimeType.startsWith("image/")) {
                    return MessageType.IMAGE;
                }

                if (mimeType.startsWith("video/")) {
                    return MessageType.VIDEO;
                }

                if (mimeType.startsWith("audio/")) {
                    return MessageType.AUDIO;
                }
            }

            return MessageType.FILE;
        }

        return MessageType.TEXT;
    }
}