package com.rental_api.ServiceBooking.Dto.Request;

import lombok.Data;

@Data
public class ProviderRequestDto {
    private Long userId;       // The user requesting
    private String bio;        // Short bio
    private Double experience; // Use Double for fractional years
}
