package com.rental_api.ServiceBooking.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.*;

@Entity
@Table(name = "open_classes")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class OpenClass {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    
    @Column(length = 1000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_id")
    private Tutor tutor;

    // --- ADD THIS FIELD TO FIX THE ERROR ---
    @ManyToMany
    @JoinTable(
        name = "class_subjects",
        joinColumns = @JoinColumn(name = "class_id"),
        inverseJoinColumns = @JoinColumn(name = "subject_id")
    )
    private List<Subject> subjects; 
    // ---------------------------------------

    @Enumerated(EnumType.STRING)
    private ClassStatus status;

    @ElementCollection
    @MapKeyColumn(name = "student_count")
    @Column(name = "price")
    private Map<Integer, BigDecimal> priceOptions;

    @ElementCollection(targetClass = LearningMode.class)
    @Enumerated(EnumType.STRING)
    private Set<LearningMode> learningModes;

    private String city;
    private String district;
    private String address;

    @OneToMany(mappedBy = "openClass", cascade = CascadeType.ALL)
    private List<ClassSchedule> schedules;

    public enum LearningMode { STUDENT_HOME, TUTOR_HOME, ONLINE, OUTSIDE }
    public enum ClassStatus { OPEN, CLOSED, FULL, ARCHIVED }
}