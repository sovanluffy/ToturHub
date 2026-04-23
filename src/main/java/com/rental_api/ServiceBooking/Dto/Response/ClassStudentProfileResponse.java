package com.rental_api.ServiceBooking.Dto.Response;

import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClassStudentProfileResponse {

    private Long classId;
    private String classTitle;

    private int totalStudents;

    private List<StudentInfo> students;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StudentInfo {
        private Long userId;
        private String name;
        private String email;
        private String phone;
        private String avatar;
    }
}