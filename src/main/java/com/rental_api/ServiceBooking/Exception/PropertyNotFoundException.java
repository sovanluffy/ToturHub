package com.rental_api.ServiceBooking.Exception;

public class PropertyNotFoundException extends RuntimeException {
    
    public PropertyNotFoundException() {
        super();
    }

    public PropertyNotFoundException(String message) {
        super(message);
    }

    public PropertyNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
