package com.rental_api.ServiceBooking.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationMessage {

    private String type;
    private String content;

    private Long bookingId;
    private Long classId;

    // 🔥 NEW UI FIELDS
    private String studentName;
    private String classTitle;

    private String day;
    private String startTime;
    private String endTime;

    private String telegram;
}