package com.rental_api.ServiceBooking.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;

@Entity
@Table(name = "day_time_slots")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DayTimeSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private DayOfWeek day;

    // ✅ FIX: use String instead of LocalTime
    private String startTime; // "08:00"
    private String endTime; // "10:00"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id")
    private OpenClass openClass;
}