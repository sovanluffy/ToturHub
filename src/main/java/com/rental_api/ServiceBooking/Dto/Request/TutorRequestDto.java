package com.rental_api.ServiceBooking.Dto.Request;

import lombok.*;
import java.math.BigDecimal;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TutorRequestDto {
    private Long userId;          // ID of the existing User account
    private String bio;
    private Integer experienceYears;
    private BigDecimal pricePerHour;
    private String telegram;
    private Set<Long> subjectIds; // List of Subject IDs the tutor teaches
}