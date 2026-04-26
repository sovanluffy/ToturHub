package com.rental_api.ServiceBooking.Dto.Response;

import java.time.LocalDateTime;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReviewResponse {
    private Long id;
    private String comment;
    private Integer rating;
    
    // Identifiers
    private Long tutorId;
    private Long studentId;
    private Long classId; // 🔥 Added to track which class was reviewed
    
    // UI Display Fields
    private String studentName;
    private String studentAvatar;
    private String classTitle; // 🔥 Added to show the class name in lists
    
    private LocalDateTime createdAt;
}