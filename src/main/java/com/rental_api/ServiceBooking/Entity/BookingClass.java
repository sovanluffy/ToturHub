package com.rental_api.ServiceBooking.Entity;

import com.rental_api.ServiceBooking.Entity.Enum.BookingStatus;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "bookings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The class being bookedkhmer
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    private OpenClass openClass;

    // The specific time/date configuration chosen12
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_config_id", nullable = false)
    private ScheduleConfig scheduleConfig;

    // The Student (Mapped to User entity)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User user;

    // The Tutor12
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_id", nullable = false)
    private Tutor tutor;

    @Column(length = 2000)
    private String note;

    @Column(length = 255, nullable = false)
    private String telegram;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * Automatically sets the timestamp and default status before saving to DB
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = BookingStatus.PENDING;
        }
    }
}