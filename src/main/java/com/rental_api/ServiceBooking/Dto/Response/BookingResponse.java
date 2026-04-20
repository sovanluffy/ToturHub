package com.rental_api.ServiceBooking.Dto.Response;

import com.rental_api.ServiceBooking.Entity.Enum.BookingStatus;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookingResponse {

    private Long bookingId;

    // class info
    private Long classId;
    private String classTitle;

    // schedule info (DayTimeSlot)
    private Long scheduleId;
    private DayOfWeek day;
    private LocalTime startTime;
    private LocalTime endTime;

    // booking info
    private BookingStatus status;
    private String note;
    private String telegram;
    private LocalDateTime createdAt;
}