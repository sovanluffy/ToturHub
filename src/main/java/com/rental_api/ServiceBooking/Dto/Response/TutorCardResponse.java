package com.rental_api.ServiceBooking.Dto.Response;

import lombok.*;
import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TutorCardResponse {
    private Long classId;
    private String title;
    private String tutorName;
    private String tutorImage;
    private Double rating;
    private String location; 
    private Map<Integer, BigDecimal> priceOptions;
}