package com.rental_api.ServiceBooking.Dto.Response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationResponse {
    private Long locationId;
    private String city;
    private String district;
    private String fullAddress;
}