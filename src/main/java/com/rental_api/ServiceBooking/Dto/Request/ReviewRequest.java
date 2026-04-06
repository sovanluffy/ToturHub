package com.rental_api.ServiceBooking.Dto.Request;

import lombok.Data;

@Data
public class ReviewRequest {
    private String comment;
    private Integer rating; 
}
