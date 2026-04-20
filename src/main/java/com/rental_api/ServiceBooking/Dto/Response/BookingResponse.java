package com.rental_api.ServiceBooking.Dto.Response;

import com.rental_api.ServiceBooking.Entity.Enum.BookingStatus;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookingResponse {

    private Long bookingId;

    // ✅ USER
    private Long userId;

    // ✅ CLASS
    private Long classId;
    private String classTitle;

    // ✅ SCHEDULE
    private Long scheduleId;
    private DayOfWeek day;
    private LocalTime startTime;
    private LocalTime endTime;

    // ✅ BOOKING INFO
    private BookingStatus status;
    private String note;
    private String telegram;
    private LocalDateTime createdAt;
}