package com.rental_api.ServiceBooking.Services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.rental_api.ServiceBooking.Dto.Request.BookingClassRequest;
import com.rental_api.ServiceBooking.Dto.Response.BookingResponse;

@Service
public interface BookingService {

    BookingResponse bookClass(Long openClassId,BookingClassRequest request);
    List<BookingResponse> getBookingsByClassId(Long openClassId);
    List<BookingResponse> getBookingsByUserId(Long userId);
    BookingResponse conformBooking(Long bookingId);
    BookingResponse rejectBooking(Long bookingId);
}
