package com.rental_api.ServiceBooking.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "open_classes")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class OpenClass {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;
    
    @Column(length = 2000) // Increased length for detailed descriptions
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_id", nullable = false)
    private Tutor tutor;

    // --- 📚 SUBJECT FILTERS ---
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "class_subjects",
        joinColumns = @JoinColumn(name = "class_id"),
        inverseJoinColumns = @JoinColumn(name = "subject_id")
    )
    private List<Subject> subjects = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private ClassStatus status = ClassStatus.OPEN;

    // --- 💰 PRICE FILTERS ---
    // Key: Student Count (e.g., 1 for private, 5 for group)
    // Value: Price per student
    @ElementCollection
    @CollectionTable(name = "class_pricing", joinColumns = @JoinColumn(name = "class_id"))
    @MapKeyColumn(name = "student_group_size")
    @Column(name = "price_amount")
    private Map<Integer, BigDecimal> priceOptions;

    // --- 📍 LOCATION FILTERS ---
    @ElementCollection(targetClass = LearningMode.class)
    @CollectionTable(name = "class_learning_modes", joinColumns = @JoinColumn(name = "class_id"))
    @Enumerated(EnumType.STRING)
    private Set<LearningMode> learningModes;

    private String city;
    private String district;
    private String address;

    // --- 📅 AVAILABILITY FILTERS ---
    @OneToMany(mappedBy = "openClass", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ClassSchedule> schedules = new ArrayList<>();

    // --- 🕒 METADATA ---
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // ENUMS
    public enum LearningMode { STUDENT_HOME, TUTOR_HOME, ONLINE, OUTSIDE }
    public enum ClassStatus { OPEN, CLOSED, FULL, ARCHIVED }
}