// UnauthorizedException.java
package com.rental_api.ServiceBooking.Exception;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
