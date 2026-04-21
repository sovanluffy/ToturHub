package com.rental_api.ServiceBooking.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationMessage {

    // ================= BASIC EVENT =================
    private String type;
    private String content;

    private Long bookingId;
    private Long classId;

    // ================= USER PROFILE =================
    private Long userId;
    private String fullname;
    private String email;
    private String phone;
    private String avatarUrl;

    // ================= AUTH =================
    private String token;
    private List<String> roles;
    private List<Long> roleIds;

    // ================= TUTOR INFO =================
    private Long tutorId;

    // ================= LOCATION =================
    private Long locationId;
    private String city;
    private String district;
    private String fullAddress;

    // ================= BOOKING CONTEXT =================
    private String message;
    private String studentName;
    private String classTitle;

    private String day;
    private String startTime;
    private String endTime;

    private String telegram;
}