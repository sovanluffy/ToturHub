package com.rental_api.ServiceBooking.Entity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter // Explicitly add this
@Setter // Explicitly add this
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Ensure this is EXACTLY 'scheduleType'
    private String scheduleType;

    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime startTime;
    private LocalTime endTime;

    @ManyToOne
    @JoinColumn(name = "open_class_id")
    private OpenClass openClass;

    @OneToMany(mappedBy = "scheduleConfig", fetch = FetchType.LAZY)
    @Builder.Default
    private List<BookingClass> bookings = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "schedule_config_times", joinColumns = @JoinColumn(name = "config_id"))
    private List<TimeRange> timeRanges = new ArrayList<>();

    @OneToMany(mappedBy = "config", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ClassSchedule> individualSlots = new ArrayList<>();
}