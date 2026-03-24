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

    /**
     * Processes Facebook login using the access token from the JS SDK.
     */
    AuthResponse loginWithFacebook(String accessToken);

    /**
     * Handles the callback when a user removes the app from their Facebook settings.
     * This is required by Facebook's Data Deletion Policy.
     */
    void handleFacebookDeauthorize(String signedRequest);
}