package com.rental_api.ServiceBooking.Dto.Request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {

    @NotBlank(message = "Fullname is required")
    private String fullname;

    @NotBlank(message = "Email is required")
    @Email(message = "Email is invalid")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    private String phone;
    private String address;
    private String location;

    // Do NOT include avatarUrl here
    // File upload is handled in Controller with MultipartFile
}