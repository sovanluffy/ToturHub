package com.rental_api.ServiceBooking.Dto.Request;

import lombok.Data;

@Data
public class bookingClassRequest {     
    private Long scheduleId;          
    private String note;
}
