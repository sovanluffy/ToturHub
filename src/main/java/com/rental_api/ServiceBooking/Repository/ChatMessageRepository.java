package com.rental_api.ServiceBooking.Repository;

import com.rental_api.ServiceBooking.Entity.ChatMessage;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByRecipient_IdAndSender_IdAndIsReadFalse(Long recipientId, Long senderId);

    @Query("""
        SELECT m FROM ChatMessage m
        WHERE (m.sender.id = :u1 AND m.recipient.id = :u2)
           OR (m.sender.id = :u2 AND m.recipient.id = :u1)
        ORDER BY m.timestamp ASC
    """)
    List<ChatMessage> findChatHistory(@Param("u1") Long user1,
                                      @Param("u2") Long user2);
}