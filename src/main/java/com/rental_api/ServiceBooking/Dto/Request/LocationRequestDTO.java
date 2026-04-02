package com.rental_api.ServiceBooking.Dto.Response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationRequestDTO {
    private String city;
    private String district;
    private String address;
}