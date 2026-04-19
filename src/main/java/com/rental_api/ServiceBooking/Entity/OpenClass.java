package com.rental_api.ServiceBooking.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "open_classes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpenClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ================= BASIC =================
    private String title;

    @Column(length = 2000)
    private String description;

    // ================= IMAGE =================
    private String classImage;

    // ================= TUTOR =================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_id", nullable = false)
    private Tutor tutor;

    // ================= SUBJECTS =================
    @ManyToMany
    @JoinTable(name = "class_subjects", joinColumns = @JoinColumn(name = "class_id"), inverseJoinColumns = @JoinColumn(name = "subject_id"))
    private List<Subject> subjects = new ArrayList<>();

    // ================= STATUS =================
    @Enumerated(EnumType.STRING)
    private ClassStatus status = ClassStatus.OPEN;

    // ================= PRICE =================
    private BigDecimal basePrice;
    private Integer maxStudents;

    // ================= LOCATION =================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    private String specificAddress;

    // ================= LEARNING MODE =================
    @ElementCollection
    @Enumerated(EnumType.STRING)
    private Set<LearningMode> learningModes = new HashSet<>();

    // ================= SCHEDULE =================
    @ElementCollection
    @CollectionTable(name = "class_day_time_slots", joinColumns = @JoinColumn(name = "class_id"))
    private List<DayTimeSlot> dayTimeSlots = new ArrayList<>();

    // ================= META =================
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // ================= ENUMS =================

    public enum ClassStatus {
        OPEN,
        CLOSED,
        FULL,
        ARCHIVED
    }

    public enum LearningMode {
        ONLINE,
        STUDENT_HOME,
        TUTOR_CLASS,
        OUTSIDE
    }
}