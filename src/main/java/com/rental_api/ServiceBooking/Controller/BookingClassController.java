package com.rental_api.ServiceBooking.Controller;

import com.rental_api.ServiceBooking.Dto.Request.BookingClassRequest;
import com.rental_api.ServiceBooking.Dto.Response.BookingResponse;
import com.rental_api.ServiceBooking.Services.BookingService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingClassController {

    private final BookingService bookingService;

    // =====================================================
    // 🔹 STUDENT ACTIONS
    // =====================================================

    /**
     * Student books a class
     */
    @PostMapping("/book-class/{openClassId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<BookingResponse> bookClass(
            @PathVariable Long openClassId,
            @RequestBody BookingClassRequest request) {
        BookingResponse response = bookingService.bookClass(openClassId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get booking history for a specific user
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('STUDENT','ADMIN') and @securityService.isSelf(#userId, authentication.name)")
    public ResponseEntity<List<BookingResponse>> getBookingsByUserId(
            @PathVariable Long userId) {
        List<BookingResponse> response = bookingService.getBookingsByUserId(userId);
        return ResponseEntity.ok(response);
    }

    // =====================================================
    // 🔹 TUTOR ACTIONS
    // =====================================================

    /**
     * Get bookings for a specific class (Tutor view)
     */
    @GetMapping("/class/{openClassId}")
    @PreAuthorize("hasRole('TUTOR') and @securityService.isTutorOfClass(authentication, #openClassId)")
    public ResponseEntity<List<BookingResponse>> getBookingsByClassId(
            @PathVariable Long openClassId) {
        List<BookingResponse> response = bookingService.getBookingsByClassId(openClassId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all bookings for a tutor (Dashboard)
     */
    @GetMapping("/tutor/{tutorId}")
    @PreAuthorize("hasRole('TUTOR') and @securityService.isSelfTutor(#tutorId, authentication.name)")
    public ResponseEntity<List<BookingResponse>> getBookingsByTutorId(
            @PathVariable Long tutorId) {
        List<BookingResponse> response = bookingService.getBookingsByTutorId(tutorId);
        return ResponseEntity.ok(response);
    }

    /**
     * Tutor confirms booking
     */
    @PatchMapping("/confirm/{bookingId}")
    @PreAuthorize("hasRole('TUTOR') and @securityService.isTutorOfBooking(authentication, #bookingId)")
    public ResponseEntity<BookingResponse> confirmBooking(
            @PathVariable Long bookingId) {
        BookingResponse response = bookingService.confirmBooking(bookingId);
        return ResponseEntity.ok(response);
    }

    /**
     * Tutor rejects booking
     */
    @PatchMapping("/reject/{bookingId}")
    @PreAuthorize("hasRole('TUTOR') and @securityService.isTutorOfBooking(authentication, #bookingId)")
    public ResponseEntity<BookingResponse> rejectBooking(
            @PathVariable Long bookingId) {
        BookingResponse response = bookingService.rejectBooking(bookingId);
        return ResponseEntity.ok(response);
    }
}