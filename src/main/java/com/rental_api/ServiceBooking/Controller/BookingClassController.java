package com.rental_api.ServiceBooking.Controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.rental_api.ServiceBooking.Dto.Request.BookingClassRequest;
import com.rental_api.ServiceBooking.Dto.Response.BookingResponse;
import com.rental_api.ServiceBooking.Services.BookingService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingClassController {
    
    private final BookingService bookingService;

    // --- STUDENT ACTIONS ---

    @PostMapping("/book-class/{openClassId}")
    @PreAuthorize("hasRole('STUDENT')") // Standardized to UPPERCASE to match JWT
    public ResponseEntity<BookingResponse> bookClass(
            @PathVariable Long openClassId, 
            @RequestBody BookingClassRequest request) {
        return ResponseEntity.ok(bookingService.bookClass(openClassId, request));
    }

    @GetMapping("/user/{userId}")
    // Checks if the logged-in user is viewing their own history
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN') and @securityService.isSelf(#userId, authentication.name)")   
    public ResponseEntity<List<BookingResponse>> getBookingsByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(bookingService.getBookingsByUserId(userId));
    }

    // --- TUTOR ACTIONS ---

    /**
     * Get bookings for a specific class ID
     */
    @GetMapping("/class/{openClassId}")
    @PreAuthorize("hasRole('TUTOR') and @securityService.isTutorOfClass(authentication, #openClassId)") 
    public ResponseEntity<List<BookingResponse>> getBookingsByClassId(@PathVariable Long openClassId) {
        return ResponseEntity.ok(bookingService.getBookingsByClassId(openClassId));
    }

    /**
     * Get all bookings for a specific tutor (Dashboard view)
     */
    @GetMapping("/tutor/{tutorId}")
    @PreAuthorize("hasRole('TUTOR') and @securityService.isSelfTutor(#tutorId, authentication.name)")
    public ResponseEntity<List<BookingResponse>> getBookingsByTutorId(@PathVariable Long tutorId) {
        return ResponseEntity.ok(bookingService.getBookingsByTutorId(tutorId));
    }

    @PatchMapping("/confirm/{bookingId}")
    @PreAuthorize("hasRole('TUTOR') and @securityService.isTutorOfBooking(authentication, #bookingId)") 
    public ResponseEntity<BookingResponse> confirmBooking(@PathVariable Long bookingId) {
        return ResponseEntity.ok(bookingService.confirmBooking(bookingId));
    }

    @PatchMapping("/reject/{bookingId}")
    @PreAuthorize("hasRole('TUTOR') and @securityService.isTutorOfBooking(authentication, #bookingId)") 
    public ResponseEntity<BookingResponse> rejectBooking(@PathVariable Long bookingId) {
        return ResponseEntity.ok(bookingService.rejectBooking(bookingId));
    }
}