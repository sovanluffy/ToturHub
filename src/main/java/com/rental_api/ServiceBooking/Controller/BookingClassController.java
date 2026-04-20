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

    // ================= STUDENT =================

    @PostMapping("/book-class/{openClassId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<BookingResponse> bookClass(
            @PathVariable Long openClassId,
            @RequestBody BookingClassRequest request) {

        return ResponseEntity.ok(
                bookingService.bookClass(openClassId, request)
        );
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<BookingResponse>> getBookingsByUserId(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                bookingService.getBookingsByUserId(userId)
        );
    }

    // ================= TUTOR =================

    /**
     * ✅ MAIN FIXED ENDPOINT (use this)
     */
    @GetMapping("/tutor/{tutorId}")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<List<BookingResponse>> getBookingsByTutorId(
            @PathVariable Long tutorId) {

        return ResponseEntity.ok(
                bookingService.getBookingsByTutorId(tutorId)
        );
    }

    // ================= CLASS =================

    @GetMapping("/class/{openClassId}")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<List<BookingResponse>> getBookingsByClassId(
            @PathVariable Long openClassId) {

        return ResponseEntity.ok(
                bookingService.getBookingsByClassId(openClassId)
        );
    }

    // ================= ACTIONS =================

    @PatchMapping("/confirm/{bookingId}")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<BookingResponse> confirmBooking(
            @PathVariable Long bookingId) {

        return ResponseEntity.ok(
                bookingService.confirmBooking(bookingId)
        );
    }

    @PatchMapping("/reject/{bookingId}")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<BookingResponse> rejectBooking(
            @PathVariable Long bookingId) {

        return ResponseEntity.ok(
                bookingService.rejectBooking(bookingId)
        );
    }
}