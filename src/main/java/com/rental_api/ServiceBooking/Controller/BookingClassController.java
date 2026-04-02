package com.rental_api.ServiceBooking.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.rental_api.ServiceBooking.Dto.Request.BookingClassRequest;
import com.rental_api.ServiceBooking.Dto.Response.BookingResponse;
import com.rental_api.ServiceBooking.Services.BookingService;

import lombok.RequiredArgsConstructor;

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

}
