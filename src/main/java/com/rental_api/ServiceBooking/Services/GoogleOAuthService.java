package com.rental_api.ServiceBooking.Services;

import com.rental_api.ServiceBooking.Dto.Response.AuthResponse;

/**
 * Service interface for handling OAuth2 authentication with Google and Facebook.
 */
public interface GoogleOAuthService {

    /**
     * Processes Google login using the authorization code.
     */
    AuthResponse loginWithGoogle(String code);

 
}