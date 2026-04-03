package com.rental_api.ServiceBooking.Dto.Response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ProfileResponse {
    private Long userId;
    private String fullname;
    private String email;
    private String phone;
    private String address;
    private String avatarUrl;
    private String status;
    private List<String> roles;
private Long locationId;
private String city;
private String district;
private String fullAddress;}