package com.rental_api.ServiceBooking.Dto.Response;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private Long userId;
    private String fullname;
    private String email;
    private String phone;     // Added this field
    private String avatarUrl;
    private String message;
    private String token;
    private List<String> roles;
    private List<Long> roleIds;
}