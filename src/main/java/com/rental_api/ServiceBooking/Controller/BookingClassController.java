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

    // ================= BOOK CLASS =================
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

    // ================= GET BOOKINGS BY USER ID =================
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TUTOR')")
    public ResponseEntity<List<BookingResponse>> getBookingsByUserId(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(
                bookingService.getBookingsByUserId(userId)
        );
    }

    // ================= MY BOOKINGS (STUDENT) =================
    @GetMapping("/user/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<BookingResponse>> getMyBookings() {
        return ResponseEntity.ok(
                bookingService.getMyBookings()
        );
    }

    // ================= MY BOOKINGS (TUTOR) =================
    @GetMapping("/tutor/me")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<List<BookingResponse>> getMyTutorBookings() {
        return ResponseEntity.ok(
                bookingService.getMyTutorBookings()
        );
    }

    // ================= CLASS BOOKINGS =================
    @GetMapping("/class/{classId}")
    @PreAuthorize("hasRole('TUTOR') or hasRole('ADMIN')")
    public ResponseEntity<List<BookingResponse>> getBookingsByClassId(
            @PathVariable Long classId
    ) {
        return ResponseEntity.ok(
                bookingService.getBookingsByClassId(classId)
        );
    }

    // ================= PENDING COUNT (BADGE) =================
    @GetMapping("/tutor/me/pending-count")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<Long> getPendingCount() {
        return ResponseEntity.ok(
                bookingService.getMyPendingBookingsCount()
        );
    }

    // ================= OPTIONAL (RECOMMENDED) =================

    // GET BOOKING DETAILS BY USER (safe view)
    @GetMapping("/user/{userId}/detail")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TUTOR')")
    public ResponseEntity<List<BookingResponse>> getUserBookingDetail(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(
                bookingService.getBookingsByUserId(userId)
        );
    }

    // GET MY BOOKINGS (GENERAL ENDPOINT FOR FRONTEND)
    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TUTOR')")
    public ResponseEntity<List<BookingResponse>> getMyAllBookings() {
        return ResponseEntity.ok(
                bookingService.getMyBookings()
        );
    }
}