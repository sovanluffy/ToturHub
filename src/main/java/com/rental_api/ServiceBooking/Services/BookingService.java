package com.rental_api.ServiceBooking.Services;

import org.springframework.stereotype.Service;

import com.rental_api.ServiceBooking.Dto.Request.BookingClassRequest;
import com.rental_api.ServiceBooking.Dto.Response.BookingResponse;

@Service
public interface BookingService {

    BookingResponse bookClass(Long openClassId,BookingClassRequest request);
}
