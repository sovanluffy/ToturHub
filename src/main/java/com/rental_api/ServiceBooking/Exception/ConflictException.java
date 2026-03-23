// ConflictException.java
package com.rental_api.ServiceBooking.Exception;

public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
