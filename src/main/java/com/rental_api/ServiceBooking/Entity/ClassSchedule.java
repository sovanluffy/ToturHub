package com.rental_api.ServiceBooking.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClassSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // ✅ Match the existing database column name exactly
    @Builder.Default
    @Column(name = "is_booked", nullable = false)
    private boolean booked = false;

    @Enumerated(EnumType.STRING)
    private ScheduleType type;

    @ManyToOne
    @JoinColumn(name = "open_class_id")
    private OpenClass openClass;

    @ManyToOne
    @JoinColumn(name = "schedule_config_id")
    private ScheduleConfig config;

    public enum ScheduleType {
        DAILY, WEEKEND, ONCE
    }
}