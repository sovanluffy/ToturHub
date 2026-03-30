package com.rental_api.ServiceBooking.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // Add these to handle Daily vs Weekend
    @Enumerated(EnumType.STRING)
    private ScheduleType type; // DAILY, WEEKEND, SPECIFIC_DATE

    private boolean isBooked = false;

    @ManyToOne
    @JoinColumn(name = "open_class_id")
    private OpenClass openClass;

    public enum ScheduleType {
        DAILY, WEEKEND, ONCE
    }
}