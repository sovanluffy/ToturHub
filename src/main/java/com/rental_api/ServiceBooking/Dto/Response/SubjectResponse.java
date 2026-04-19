package com.rental_api.ServiceBooking.Dto.Response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubjectResponse {
    private Long id;
    private String name;
}