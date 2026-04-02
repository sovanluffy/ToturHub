package com.rental_api.ServiceBooking.Entity;



import com.rental_api.ServiceBooking.Entity.Enum.BookingStatus;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "bookings")
@Data
@Getter
@Setter
@NoArgsConstructor // Good practice to be explicit
@AllArgsConstructor
public class BookingClass {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    private OpenClass openClass;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_config_id", nullable = false)
    private ScheduleConfig scheduleConfig; 

    // FIX 1: Satisfy the "student_id" constraint
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false) 
    private User user;

    // FIX 2: Satisfy the "tutor_id" constraint
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_id", nullable = false)
    private Tutor tutor;

    @Column(length = 2000)
    private String note;
    
    @Enumerated(EnumType.STRING)
    private BookingStatus status; 

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}