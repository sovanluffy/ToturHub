package com.rental_api.ServiceBooking.Services;

import com.rental_api.ServiceBooking.Dto.ChatContactResponse;
import com.rental_api.ServiceBooking.Dto.ChatRequest;
import com.rental_api.ServiceBooking.Dto.ChatResponse;
import com.rental_api.ServiceBooking.Dto.Request.BookingClassRequest;
import com.rental_api.ServiceBooking.Dto.Response.BookingResponse;

import java.util.List;

public interface BookingService {

    // =====================================================
    // ================= BOOKING CORE ======================
    // =====================================================

    BookingResponse bookClass(Long openClassId, BookingClassRequest request);

    BookingResponse confirmBooking(Long bookingId);

    BookingResponse rejectBooking(Long bookingId);

    // =====================================================
    // ================= BOOKING QUERIES ===================
    // =====================================================

    List<BookingResponse> getBookingsByUserId(Long userId);

    List<BookingResponse> getBookingsByClassId(Long classId);

    List<BookingResponse> getBookingsByTutorId(Long tutorId);

    List<BookingResponse> getMyBookings();

    List<BookingResponse> getMyTutorBookings();

    Long getMyPendingBookingsCount();

    // =====================================================
    // ================= CHAT SYSTEM =======================
    // =====================================================

    ChatResponse sendMessage(String senderEmail, ChatRequest request);

    List<ChatResponse> getChatHistory(String myEmail, Long otherUserId);

    void markMessagesAsRead(String recipientEmail, Long senderId);

    Long getUnreadMessageCount(String email);

    // =====================================================
    // ================= CHAT CONTACTS =====================
    // =====================================================

    List<ChatContactResponse> getChatContacts(String email);

    List<Long> getChatUserList(String email);
}