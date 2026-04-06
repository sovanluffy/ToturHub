package com.rental_api.ServiceBooking.Dto.Response;

import lombok.*;
import java.util.ArrayList;
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
    private String bio;
    private List<String> subjects;
    private String location;
    private Integer totalOpenClasses;

    // ✅ MANUAL CONSTRUCTOR: Matches the 6-field JPQL query exactly
    public TutorCardResponse(Long tutorId, String fullname, String profilePicture, 
                             Double rating, Integer studentsTaught, String bio) {
        this.tutorId = tutorId;
        this.fullname = fullname;
        this.profilePicture = profilePicture;
        this.rating = (rating != null) ? rating : 0.0;
        this.studentsTaught = (studentsTaught != null) ? studentsTaught : 0;
        this.bio = bio;
        // Default values for fields not in the basic "Card" query
        this.subjects = new ArrayList<>();
        this.location = "Remote";
        this.totalOpenClasses = 0;
    }
}