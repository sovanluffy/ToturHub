package com.rental_api.ServiceBooking.Dto.Request;

import lombok.Data;

@Data
public class BookingClassRequest {

    private Long dayTimeSlotId;

    private String telegram;
    private String note;
}