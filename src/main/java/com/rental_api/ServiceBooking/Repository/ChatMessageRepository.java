package com.rental_api.ServiceBooking.Repository;

import com.rental_api.ServiceBooking.Entity.ChatMessage;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // =====================================================
    // ================= UNREAD MESSAGES ===================
    // =====================================================

    List<ChatMessage> findByRecipient_IdAndSender_IdAndReadFalse(
            Long recipientId,
            Long senderId
    );

    // Optional safer version (recommended naming)
    // List<ChatMessage> findByRecipientIdAndSenderIdAndReadFalse(
    //        Long recipientId,
    //        Long senderId
    // );

    // =====================================================
    // ================= COUNT UNREAD ======================
    // =====================================================

    @Query("""
        SELECT COUNT(c)
        FROM ChatMessage c
        WHERE c.recipient.id = :userId
        AND c.read = false
    """)
    Long countUnreadByRecipient(@Param("userId") Long userId);

    // =====================================================
    // ================= CHAT HISTORY ======================
    // =====================================================

    @Query("""
        SELECT m FROM ChatMessage m
        WHERE (m.sender.id = :u1 AND m.recipient.id = :u2)
           OR (m.sender.id = :u2 AND m.recipient.id = :u1)
        ORDER BY m.timestamp ASC
    """)
    List<ChatMessage> findChatHistory(
            @Param("u1") Long user1,
            @Param("u2") Long user2
    );

    // =====================================================
    // ================= ALL USER MESSAGES =================
    // =====================================================

    @Query("""
        SELECT m FROM ChatMessage m
        WHERE m.sender.id = :userId
           OR m.recipient.id = :userId
        ORDER BY m.timestamp DESC
    """)
    List<ChatMessage> findAllUserMessages(@Param("userId") Long userId);

    // =====================================================
    // ========== LATEST MESSAGE PER CHAT ==================
    // =====================================================

    @Query("""
        SELECT m FROM ChatMessage m
        WHERE m.timestamp = (
            SELECT MAX(m2.timestamp)
            FROM ChatMessage m2
            WHERE (m2.sender.id = m.sender.id AND m2.recipient.id = m.recipient.id)
               OR (m2.sender.id = m.recipient.id AND m2.recipient.id = m.sender.id)
        )
        AND (m.sender.id = :userId OR m.recipient.id = :userId)
        ORDER BY m.timestamp DESC
    """)
    List<ChatMessage> findLatestChats(@Param("userId") Long userId);
}