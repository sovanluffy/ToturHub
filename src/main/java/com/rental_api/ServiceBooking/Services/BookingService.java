package com.rental_api.ServiceBooking.Services;

import com.rental_api.ServiceBooking.Dto.ChatRequest;
import com.rental_api.ServiceBooking.Dto.ChatResponse;
import com.rental_api.ServiceBooking.Dto.Request.BookingClassRequest;
import com.rental_api.ServiceBooking.Dto.Response.BookingResponse;

import java.util.List;

public interface BookingService {

    /* ================= BOOKING CORE ================= */

    BookingResponse bookClass(Long openClassId, BookingClassRequest request);

    BookingResponse confirmBooking(Long bookingId);

    BookingResponse rejectBooking(Long bookingId);

    /* ================= BOOKING GETTERS ================= */

    List<BookingResponse> getBookingsByUserId(Long userId);

    List<BookingResponse> getBookingsByClassId(Long classId);

    List<BookingResponse> getBookingsByTutorId(Long tutorId);

    List<BookingResponse> getMyBookings();

    List<BookingResponse> getMyTutorBookings();
Long getUnreadMessageCount(String email);
    Long getMyPendingBookingsCount();

    /* ================= CHAT SYSTEM ================= */

    /**
     * Save message in DB + return saved message
     * (WebSocket layer will broadcast it)
     */
    ChatResponse sendMessage(String senderEmail, ChatRequest request);

    /**
     * Get chat history between current user and other user
     * ALWAYS returns DB data (source of truth)
     */
    List<ChatResponse> getChatHistory(String myEmail, Long otherUserId);

    /**
     * Mark messages as read when user opens chat
     */
    void markMessagesAsRead(String recipientEmail, Long senderId);
}