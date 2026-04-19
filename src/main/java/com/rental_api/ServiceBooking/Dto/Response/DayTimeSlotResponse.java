package com.rental_api.ServiceBooking.Dto.Response;

import lombok.*;

@Data
@Builder
public class DayTimeSlotResponse {

    private String day;
    private String startTime;
    private String endTime;
}