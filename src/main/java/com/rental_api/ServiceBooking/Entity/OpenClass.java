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

    private String title;

    @Column(length = 2000)
    private String description;

    private String classImage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_id", nullable = false)
    private Tutor tutor;

    @ManyToMany
    @JoinTable(name = "class_subjects", joinColumns = @JoinColumn(name = "class_id"), inverseJoinColumns = @JoinColumn(name = "subject_id"))
    private List<Subject> subjects = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private ClassStatus status = ClassStatus.OPEN;

    private BigDecimal basePrice;
    private Integer maxStudents;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    private String specificAddress;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "class_learning_modes", joinColumns = @JoinColumn(name = "class_id"))
    @Enumerated(EnumType.STRING)
    private Set<LearningMode> learningModes = new HashSet<>();

    // ✅ FIXED RELATION
    @OneToMany(mappedBy = "openClass", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DayTimeSlot> dayTimeSlots = new ArrayList<>();

    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public enum ClassStatus {
        OPEN, CLOSED, FULL, ARCHIVED
    }

    public enum LearningMode {
        ONLINE, STUDENT_HOME, TUTOR_CLASS, OUTSIDE
    }
}