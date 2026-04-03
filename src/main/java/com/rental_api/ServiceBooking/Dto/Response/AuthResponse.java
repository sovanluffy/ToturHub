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
    private String phone;
    private String avatarUrl;
    private String message;
    private String token;
    private List<String> roles;
    private List<Long> roleIds;

    // -------------------------------
    // Location info
    // -------------------------------
    private Long locationId;       // store the selected location id
    private String city;           // city of the location
    private String district;       // district of the location
    private String fullAddress;    // detailed address
}