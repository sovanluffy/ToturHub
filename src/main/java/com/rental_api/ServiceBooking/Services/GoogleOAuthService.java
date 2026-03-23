package com.rental_api.ServiceBooking.Services;

import com.rental_api.ServiceBooking.Dto.Response.AuthResponse;

public interface GoogleOAuthService {

    AuthResponse loginWithGoogle(String code);

}