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

    // ================= STUDENT BOOK CLASS =================
    @PostMapping("/book-class/{openClassId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<BookingResponse> bookClass(
            @PathVariable Long openClassId,
            @RequestBody BookingClassRequest request
    ) {
        return ResponseEntity.ok(
                bookingService.bookClass(openClassId, request)
        );
    }

    // ================= GET BOOKINGS BY USER ID =================
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('STUDENT','TUTOR','ADMIN')")
    public ResponseEntity<List<BookingResponse>> getBookingsByUserId(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(
                bookingService.getBookingsByUserId(userId)
        );
    }

    // ================= GET MY BOOKINGS (STUDENT) =================
    @GetMapping("/user/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<BookingResponse>> getMyBookings() {
        return ResponseEntity.ok(
                bookingService.getMyBookings()
        );
    }

    // ================= GET BOOKINGS BY TUTOR ID =================
    @GetMapping("/tutor/{tutorId}")
    @PreAuthorize("hasAnyRole('TUTOR','ADMIN')")
    public ResponseEntity<List<BookingResponse>> getBookingsByTutorId(
            @PathVariable Long tutorId
    ) {
        return ResponseEntity.ok(
                bookingService.getBookingsByTutorId(tutorId)
        );
    }

    // ================= TUTOR BOOKINGS (SELF) =================
    @GetMapping("/tutor/me")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<List<BookingResponse>> getMyTutorBookings() {
        return ResponseEntity.ok(
                bookingService.getMyTutorBookings()
        );
    }

    // ================= BOOKINGS BY CLASS =================
    @GetMapping("/class/{openClassId}")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<List<BookingResponse>> getBookingsByClassId(
            @PathVariable Long openClassId
    ) {
        return ResponseEntity.ok(
                bookingService.getBookingsByClassId(openClassId)
        );
    }

    // ================= CONFIRM BOOKING =================
    @PatchMapping("/confirm/{bookingId}")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<BookingResponse> confirmBooking(
            @PathVariable Long bookingId
    ) {
        return ResponseEntity.ok(
                bookingService.confirmBooking(bookingId)
        );
    }

    // ================= REJECT BOOKING =================
    @PatchMapping("/reject/{bookingId}")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<BookingResponse> rejectBooking(
            @PathVariable Long bookingId
    ) {
        return ResponseEntity.ok(
                bookingService.rejectBooking(bookingId)
        );
    }

    // ================= PENDING COUNT (SIDEBAR BADGE) =================
    @GetMapping("/tutor/me/pending-count")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<Long> getPendingCount() {
        return ResponseEntity.ok(
                bookingService.getMyPendingBookingsCount()
        );
    }
}