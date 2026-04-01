package com.rental_api.ServiceBooking.Entity;



import com.rental_api.ServiceBooking.Entity.Enum.BookingStatus;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "bookings")
@Data
public class BookingClass {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "class_id")
    @Column(name = "class_id", nullable = false)
    private OpenClass openClass;

   @ManyToOne
    @JoinColumn(name = "schedule_config_id", nullable = false) // Changed type to scheduleConfig
    private ScheduleConfig scheduleConfig; 

    @ManyToOne
    @JoinColumn(name = "user_id")
    @Column(name = "user_id", nullable = false)
    private User user;

    @Column(length = 2000, name = "note")
    private String note;
    
    @Enumerated(EnumType.STRING)
    private BookingStatus status; // PENDING, CONFIRMED, CANCELLED

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}