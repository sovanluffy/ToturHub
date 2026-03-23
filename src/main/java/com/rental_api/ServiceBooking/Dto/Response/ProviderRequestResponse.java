package com.rental_api.ServiceBooking.Dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProviderRequestResponse {
    private Long id;

    private UserInfoResponse user; // NESTED USER OBJECT

    private String bio;
    private String experience;
    private String status;
        private String message;

}
