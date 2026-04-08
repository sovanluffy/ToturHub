package com.rental_api.ServiceBooking.Services;

import java.util.List;
import com.rental_api.ServiceBooking.Dto.Request.BookingClassRequest;
import com.rental_api.ServiceBooking.Dto.Response.BookingResponse;

public interface BookingService {

    /**
     * Handles the initial booking request from a student.
     * Triggers a WebSocket notification to the Tutor.
     */
    BookingResponse bookClass(Long openClassId, BookingClassRequest request);

    /**
     * Retrieves all bookings associated with a specific class.
     */
    List<BookingResponse> getBookingsByClassId(Long openClassId);

    /**
     * Retrieves all bookings made by a specific student.
     */
    List<BookingResponse> getBookingsByUserId(Long userId);
    
    /**
     * NEW: Retrieves all bookings assigned to a specific tutor.
     * Useful for the Tutor's main dashboard.
     */
    List<BookingResponse> getBookingsByTutorId(Long tutorId);

    /**
     * Updates status to CONFIRMED.
     * Triggers a WebSocket notification to the Student.
     */
    BookingResponse confirmBooking(Long bookingId);

    /**
     * Updates status to REJECTED.
     * Triggers a WebSocket notification to the Student.
     */
    BookingResponse rejectBooking(Long bookingId);
}