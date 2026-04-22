package com.rental_api.ServiceBooking.Dto.Response;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StudentPublicResponse {

    private Long studentId;
    private String studentName;
    private String avatar;
    private LocalDateTime joinedAt;
}