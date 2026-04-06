package com.rental_api.ServiceBooking.Dto.Response;

import lombok.Data;

@Data
public class ReviewResponse {
    private Long id;
    private String comment;
    private Integer rating;
}
