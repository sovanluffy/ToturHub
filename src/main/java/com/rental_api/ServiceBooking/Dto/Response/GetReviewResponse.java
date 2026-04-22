package com.rental_api.ServiceBooking.Dto.Response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetReviewResponse {

    private Long tutorId;
    private double averageRating;
    private int totalReviewer;
    private List<ReviewResponse> reviews;
}
