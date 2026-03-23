package com.rental_api.ServiceBooking.Exception;

import lombok.Data;

@Data
public class BookingNotFoundException extends RuntimeException {
    public BookingNotFoundException(String message) {
        super(message);
    }
}
