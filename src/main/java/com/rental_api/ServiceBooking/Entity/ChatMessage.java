package com.rental_api.ServiceBooking.Entity;

import com.rental_api.ServiceBooking.Entity.Enum.MessageType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_message")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ================= USERS =================
    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;

    @ManyToOne
    @JoinColumn(name = "recipient_id")
    private User recipient;

    // ================= MESSAGE =================
    private String content;

    private boolean isRead;

    private LocalDateTime timestamp;

    // ================= TYPE =================
    @Enumerated(EnumType.STRING)
    private MessageType type;

    // ================= 🔥 BOOKING LINK (FIX) =================
    @ManyToOne
    @JoinColumn(name = "booking_id")
    private BookingClass booking;
}