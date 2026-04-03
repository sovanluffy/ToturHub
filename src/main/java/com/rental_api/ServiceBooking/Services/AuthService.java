package com.rental_api.ServiceBooking.Services;

import com.rental_api.ServiceBooking.Dto.Request.LoginRequest;
import com.rental_api.ServiceBooking.Dto.Request.RegisterRequest;
import com.rental_api.ServiceBooking.Dto.Response.AuthResponse;
import com.rental_api.ServiceBooking.Dto.Response.ProfileResponse;
import org.springframework.web.multipart.MultipartFile;

public interface AuthService {

    AuthResponse register(RegisterRequest request, MultipartFile avatar);
    AuthResponse login(LoginRequest request);
    ProfileResponse getProfile(Long userId);   // existing admin method

    // ✅ Add this for token-based profile fetching
    ProfileResponse getProfileFromToken();

    AuthResponse requestTutor();
    void approveTutor(Long userId);
    void rejectTutor(Long userId);
}