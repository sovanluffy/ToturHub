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

    // ================= TUTOR =================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_id", nullable = false)
    private Tutor tutor;

    // ================= SUBJECTS =================
    @ManyToMany
    @JoinTable(
            name = "class_subjects",
            joinColumns = @JoinColumn(name = "class_id"),
            inverseJoinColumns = @JoinColumn(name = "subject_id")
    )
    private List<Subject> subjects = new ArrayList<>();

    // ================= STATUS =================
    @Enumerated(EnumType.STRING)
    private ClassStatus status = ClassStatus.OPEN;

    // ================= VISIBILITY =================
    @Enumerated(EnumType.STRING)
    private VisibilityStatus visibilityStatus = VisibilityStatus.PUBLIC;

    // ================= PRICE =================
    private BigDecimal basePrice;
    private Integer maxStudents;

    // ================= LOCATION =================
    @ManyToOne(fetch = FetchType.LAZY)
    private Location location;

    private String specificAddress;

    // ================= LEARNING MODES =================
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "class_learning_modes", joinColumns = @JoinColumn(name = "class_id"))
    @Enumerated(EnumType.STRING)
    private Set<LearningMode> learningModes = new HashSet<>();

    // ================= SCHEDULE =================
    @OneToMany(mappedBy = "openClass", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DayTimeSlot> dayTimeSlots = new ArrayList<>();

    // ================= TIMESTAMP =================
    private LocalDateTime createdAt;
    private LocalDateTime newUntil;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.newUntil = this.createdAt.plusHours(24);
    }

    public boolean isNew() {
        return newUntil != null && LocalDateTime.now().isBefore(newUntil);
    }

    public enum ClassStatus {
        OPEN, CLOSED, FULL, ARCHIVED
    }

    public enum VisibilityStatus {
        PUBLIC, PRIVATE
    }

    public enum LearningMode {
        ONLINE, STUDENT_HOME, TUTOR_CLASS, OUTSIDE
    }
}