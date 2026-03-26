package com.rental_api.ServiceBooking.Dto.Request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class SearchRequest {
    private String city;
    private Long subjectId;
    private BigDecimal maxPrice;
    private Integer minExperience;
    private String learningMode; // String to be converted to Enum
    
    // Pagination
    private int page = 0;
    private int size = 10;
}