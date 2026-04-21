package com.rental_api.ServiceBooking.Services;

import com.rental_api.ServiceBooking.Dto.ChatRequest;
import com.rental_api.ServiceBooking.Dto.ChatResponse;
import com.rental_api.ServiceBooking.Dto.Request.BookingClassRequest;
import com.rental_api.ServiceBooking.Dto.Response.BookingResponse;

import java.util.List;

public interface BookingService {

    // ================= EXISTING BOOKING CORE =================
    BookingResponse bookClass(Long openClassId, BookingClassRequest request);
    BookingResponse confirmBooking(Long bookingId);
    BookingResponse rejectBooking(Long bookingId);

    // ================= EXISTING GETTERS =================
    List<BookingResponse> getBookingsByUserId(Long userId);
    List<BookingResponse> getBookingsByClassId(Long classId);
    List<BookingResponse> getBookingsByTutorId(Long tutorId);

    // ================= EXISTING CURRENT USER =================
    List<BookingResponse> getMyBookings();
    List<BookingResponse> getMyTutorBookings();
    Long getMyPendingBookingsCount();

    // ================= NEW: MESSENGER CHAT CORE =================
    
    /** Sends message, saves to DB, and pushes to recipient via WebSocket */
    ChatResponse sendMessage(String senderEmail, ChatRequest request);

    /** Retrieves history between current user and a specific recipient */
    List<ChatResponse> getChatHistory(String myEmail, Long otherUserId);

    /** Updates messages to "Read" status when the chat window is opened */
    void markMessagesAsRead(String recipientEmail, Long senderId);
}