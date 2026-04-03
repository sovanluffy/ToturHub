package com.rental_api.ServiceBooking.Controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.rental_api.ServiceBooking.Dto.Request.BookingClassRequest;
import com.rental_api.ServiceBooking.Dto.Response.BookingResponse;
import com.rental_api.ServiceBooking.Services.BookingService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingClassController {
    private final BookingService bookingService;

    @PostMapping("/book-class/{openClassId}")
    public ResponseEntity<BookingResponse> bookClass(@PathVariable Long openClassId, @RequestBody BookingClassRequest request) {
        BookingResponse response = bookingService.bookClass(openClassId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-class/{openClassId}")
    @PreAuthorize("hasRole('tutor') and @securityService.isTutorOfClass(authentication, #openClassId)") 
    public ResponseEntity<List<BookingResponse>> getBookingsByClassId(@PathVariable Long openClassId) {
        List<BookingResponse> responses = bookingService.getBookingsByClassId(openClassId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/my-bookings/{userId}")
    @PreAuthorize("@securityService.isSelf(#userId, authentication.name)")   
     public ResponseEntity<List<BookingResponse>> getBookingsByUserId(@PathVariable Long userId) {
        List<BookingResponse> responses = bookingService.getBookingsByUserId(userId);
        return ResponseEntity.ok(responses);
    }


    @PostMapping("/conform-booking/{bookingId}")
    @PreAuthorize("hasRole('tutor') and @securityService.isTutorOfBooking(authentication, #bookingId)") 
    public ResponseEntity<BookingResponse> conformBooking(@PathVariable Long bookingId) {
        BookingResponse response = bookingService.conformBooking(bookingId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reject-booking/{bookingId}")
    @PreAuthorize("hasRole('tutor') and @securityService.isTutorOfBooking(authentication, #bookingId)") 
    public ResponseEntity<BookingResponse> rejectBooking(@PathVariable Long bookingId) {
        BookingResponse response = bookingService.rejectBooking(bookingId);
        return ResponseEntity.ok(response);
    }
}
