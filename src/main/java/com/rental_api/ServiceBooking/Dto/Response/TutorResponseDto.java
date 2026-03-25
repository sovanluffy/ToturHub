package com.rental_api.ServiceBooking.Dto.Response;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TutorResponseDto {
    private Long id;
    private String fullname;      // From User Entity
    private String email;         // From User Entity
    private String avatarUrl;     // From User Entity
    private String bio;
    private Integer experienceYears;
    private BigDecimal pricePerHour;
    private String telegram;
    private List<String> subjects; // Just the names of the subjects
    private String city;          // From Location Entity
}