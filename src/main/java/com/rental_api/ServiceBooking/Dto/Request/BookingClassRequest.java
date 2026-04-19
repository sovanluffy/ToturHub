package com.rental_api.ServiceBooking.Dto.Request;

import lombok.Data;

@Data
public class BookingClassRequest {
    private Long scheduleId;
    private String telegram;
    private String note;
}
