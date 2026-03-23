package com.rental_api.ServiceBooking.Services;

import com.rental_api.ServiceBooking.Dto.Request.LoginRequest;
import com.rental_api.ServiceBooking.Dto.Request.RegisterRequest;
import com.rental_api.ServiceBooking.Dto.Response.AuthResponse;
import org.springframework.web.multipart.MultipartFile;

public interface AuthService {

    // ------------------- REGISTER STUDENT -------------------
    AuthResponse register(RegisterRequest request, MultipartFile avatar); // optional avatar

    // ------------------- LOGIN -------------------
    AuthResponse login(LoginRequest request);

    // ------------------- REQUEST TUTOR -------------------
    AuthResponse requestTutor(Long userId);

    // ------------------- ADMIN APPROVE / REJECT -------------------
    void approveTutor(Long userId);
    void rejectTutor(Long userId);

    // ------------------- UPLOAD AVATAR -------------------
    AuthResponse uploadAvatar(Long userId, MultipartFile file);

}