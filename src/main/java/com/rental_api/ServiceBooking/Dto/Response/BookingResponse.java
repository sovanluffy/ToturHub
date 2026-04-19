package com.rental_api.ServiceBooking.Dto.Response;

import com.rental_api.ServiceBooking.Entity.DayTimeSlot;
import com.rental_api.ServiceBooking.Entity.Enum.BookingStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookingResponse {

    private Long bookingId;
    private Long scheduleId;

    private String scheduleType;

    private LocalDate startDate;
    private LocalDate endDate;

    private List<DayTimeSlot> slots; // ✅ FIXED

    private BookingStatus status;
    private String note;
    private String telegram;
    private LocalDateTime createdAt;
}