package com.rental_api.ServiceBooking.Entity;

import com.rental_api.ServiceBooking.Entity.Enum.BookingStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "booking_class")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // student
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // tutor
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_id")
    private Tutor tutor;

    // open class
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "open_class_id")
    private OpenClass openClass;

    // ✅ FIXED: use DayTimeSlot instead of ClassSchedule
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "day_time_slot_id")
    private DayTimeSlot schedule;

    private String telegram;
    private String note;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = BookingStatus.PENDING;
        }
    }
}