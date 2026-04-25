package com.rental_api.ServiceBooking.Entity;

import com.rental_api.ServiceBooking.Entity.Enum.MessageType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "chat_message")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

    // ================= ID =================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ================= USERS =================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    // ================= TEXT =================
    @Column(columnDefinition = "TEXT")
    private String content;

    // ================= MEDIA =================
    private String mediaUrl;   // File URL (Cloudinary / S3)
    private String publicId;   // For delete file
    private String fileType;   // IMAGE / VIDEO / AUDIO

    // ================= STATUS =================
    @Column(name = "is_read")
    private boolean read;

    // ================= TIME =================
    @CreationTimestamp
    @Column(updatable = false)
    private Instant timestamp;

    // ================= MESSAGE TYPE =================
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType type;
    // TEXT / IMAGE / VIDEO / AUDIO / SYSTEM / USER

    // ================= BOOKING RELATION =================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private BookingClass booking;
}