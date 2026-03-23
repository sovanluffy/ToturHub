package com.rental_api.ServiceBooking.Dto.Request;

import lombok.Data;

@Data
public class ServiceRequest {
    private String name;
    private String description;
    private Double price;
    private Integer duration;
    private Long categoryId;
    private String imageUrl; // <-- optional image URL from client
}
