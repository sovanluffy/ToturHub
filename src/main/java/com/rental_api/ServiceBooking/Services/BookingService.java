package com.rental_api.ServiceBooking.Services;

import com.rental_api.ServiceBooking.Dto.Request.BookingClassRequest;
import com.rental_api.ServiceBooking.Dto.Response.BookingResponse;

import java.util.List;

public interface BookingService {

    // ================= CORE =================
    BookingResponse bookClass(Long openClassId, BookingClassRequest request);

    BookingResponse confirmBooking(Long bookingId);

    BookingResponse rejectBooking(Long bookingId);

    // ================= GETTERS =================
    List<BookingResponse> getBookingsByUserId(Long userId);

    List<BookingResponse> getBookingsByClassId(Long classId);

    List<BookingResponse> getBookingsByTutorId(Long tutorId);

    // ================= CURRENT USER =================
    List<BookingResponse> getMyBookings();

    List<BookingResponse> getMyTutorBookings();

    Long getMyPendingBookingsCount();
}