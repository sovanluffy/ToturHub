package com.rental_api.ServiceBooking.Services;

import com.rental_api.ServiceBooking.Dto.Request.LoginRequest;
import com.rental_api.ServiceBooking.Dto.Request.RegisterRequest;
import com.rental_api.ServiceBooking.Dto.Response.AuthResponse;
import org.springframework.web.multipart.MultipartFile;

public interface AuthService {

    AuthResponse register(RegisterRequest request, MultipartFile avatar);

    AuthResponse login(LoginRequest request);

    AuthResponse requestTutor(Long userId);

    void approveTutor(Long userId);

    void rejectTutor(Long userId);
}