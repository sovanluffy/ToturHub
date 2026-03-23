package com.rental_api.ServiceBooking.Dto.Response;

import lombok.Data;

import java.util.Set;

@Data
public class UserResponse {
    private Long id;
    private String fullname;
    private String email;
    private String phone;
    private String address;
    private String location;
    private Set<String> roles;

    public UserResponse orElseThrow(Object userNotFound) {
        return null;
    }
}
