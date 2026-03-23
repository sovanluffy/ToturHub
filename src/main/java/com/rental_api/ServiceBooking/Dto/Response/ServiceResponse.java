package com.rental_api.ServiceBooking.Dto.Response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ServiceResponse {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private Integer duration;
    private String categoryName;
    private String providerName;
    private String imageUrl; // ✅ return image URL to client
}
