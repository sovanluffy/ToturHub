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
    
    // 🔥 Added to support Class-specific viewing
    private Long classId; 
    private String classTitle; 

    // Summary Stats
    private double averageRating; // e.g., 4.5
    private int totalReviewer;    // The "Total People" count you requested
    
    // The list of detailed review items
    private List<ReviewResponse> reviews;
}