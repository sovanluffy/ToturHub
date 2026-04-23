package com.rental_api.ServiceBooking.Dto.Response;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StudentClassHistoryResponse {

    private Long bookingId;
    
    private String status;
    private String createdAt;

    // ================= CLASS =================
    private Long classId;        // ✅ HERE
    private String classTitle;
    private String classImage;

    // ================= TUTOR =================
    private Long tutorId;
    private String tutorName;
    private String tutorAvatar;

    // ================= SCHEDULE =================
    private String day;
    private String startTime;
    private String endTime;

    // ================= LOCATION =================
    private String location;
    private String specificAddress;
}