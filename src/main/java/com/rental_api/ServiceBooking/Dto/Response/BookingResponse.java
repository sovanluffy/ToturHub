package com.rental_api.ServiceBooking.Dto.Response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.rental_api.ServiceBooking.Entity.Enum.BookingStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for returning booking details to the frontend.
 * Includes flattened fields from ScheduleConfig for easier UI rendering.
 */
@Data
@Builder // Allows for easier object creation in Service layers
@AllArgsConstructor
@NoArgsConstructor
public class BookingResponse {
    
    // Primary Identifiers
    private Long bookingId;
    private Long scheduleId;
    
    // Schedule Details (Flattened from ScheduleConfig Entity)
    private String scheduleType; 
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime startTime;
    private LocalTime endTime;
    
    // Status and Metadata
    private BookingStatus status;
    private String note;
    private String telegram;
    
    // Audit Data
    private LocalDateTime createdAt; 

    /**
     * Optional: A static helper method to map Entity to DTO 
     * if you are not using a mapping library like MapStruct.
     */
    public static BookingResponse fromEntity(com.rental_api.ServiceBooking.Entity.BookingClass entity) {
        return BookingResponse.builder()
                .bookingId(entity.getId())
                .scheduleId(entity.getScheduleConfig().getId())
                .scheduleType(entity.getScheduleConfig().getScheduleType())
                .startDate(entity.getScheduleConfig().getStartDate())
                .endDate(entity.getScheduleConfig().getEndDate())
                .startTime(entity.getScheduleConfig().getStartTime())
                .endTime(entity.getScheduleConfig().getEndTime())
                .status(entity.getStatus())
                .note(entity.getNote())
                .telegram(entity.getTelegram())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}