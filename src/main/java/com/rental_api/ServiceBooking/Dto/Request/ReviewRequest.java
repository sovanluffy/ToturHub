package com.rental_api.ServiceBooking.Dto.Request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReviewRequest {
    @NotBlank(message = "Comment cannot be empty")
    private String comment;

    @Min(1)
    @Max(5)
    private Integer rating; 
}