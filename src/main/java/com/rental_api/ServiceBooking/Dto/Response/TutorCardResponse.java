package com.rental_api.ServiceBooking.Dto.Response;

import lombok.*;

import java.math.BigDecimal;
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
    private Integer totalOpenClasses; // new field
}