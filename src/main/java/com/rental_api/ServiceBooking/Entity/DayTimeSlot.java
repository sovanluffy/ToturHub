package com.rental_api.ServiceBooking.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalTime;

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

    // ✅ FIX: use LocalTime (BEST PRACTICE)
    private LocalTime startTime;
    private LocalTime endTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id")
    private OpenClass openClass;
}