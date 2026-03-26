package com.rental_api.ServiceBooking.Dto.Response;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TutorCardResponse {
    private Long tutorId;
    private String fullname;
    private String profilePicture;
    private Double rating;
    private Integer studentsTaught;
    private boolean isPublic;
    // Top 2 specialties or degrees to show on the card
    private List<String> highlights; 
    
    // Starting price for the tutor's classes
    private Double startingPrice; 
}