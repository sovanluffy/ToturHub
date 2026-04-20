package com.rental_api.ServiceBooking.Services;

import com.rental_api.ServiceBooking.Dto.Request.BookingClassRequest;
import com.rental_api.ServiceBooking.Dto.Response.BookingResponse;

import java.util.List;

public interface BookingService {

    // ================= CORE BOOKING =================
    BookingResponse bookClass(Long openClassId, BookingClassRequest request);

    BookingResponse confirmBooking(Long bookingId);

    BookingResponse rejectBooking(Long bookingId);

    // ================= BASIC GETTERS =================
    List<BookingResponse> getBookingsByUserId(Long userId);

    List<BookingResponse> getBookingsByClassId(Long classId);

    List<BookingResponse> getBookingsByTutorId(Long tutorId);

    // ================= JWT "ME" VERSION (IMPORTANT FIX) =================
    List<BookingResponse> getMyBookings();

    List<BookingResponse> getMyTutorBookings();
}