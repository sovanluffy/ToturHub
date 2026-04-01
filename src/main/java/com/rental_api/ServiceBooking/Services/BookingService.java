package com.rental_api.ServiceBooking.Services;

import org.springframework.stereotype.Service;

import com.rental_api.ServiceBooking.Dto.Request.bookingClassRequest;
import com.rental_api.ServiceBooking.Dto.Response.BookingResponse;

@Service
public interface BookingService {

    BookingResponse bookClass(Long openClassId,bookingClassRequest request, String authToken);
}
