package com.rental_api.ServiceBooking.Exception;

import lombok.Data;

@Data
public class ServiceNotFoundException extends RuntimeException {
    public ServiceNotFoundException(String message) {
        super(message);
    }
}