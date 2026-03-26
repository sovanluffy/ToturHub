package com.rental_api.ServiceBooking.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "class_schedules")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ The Parent Relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "open_class_id", nullable = false) // This maps to the foreign key in DB
    @JsonIgnore // Prevents infinite recursion during JSON serialization
    private OpenClass openClass;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Builder.Default
    private boolean isBooked = false;

    // Optional: Reference to the booking if one exists
    // @OneToOne(mappedBy = "schedule")
    // private Booking booking;
}