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
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_id")
    private Tutor tutor;

    // Multi-Price Choice: {1: 15.0, 2: 25.0}
    @ElementCollection
    @MapKeyColumn(name = "student_count")
    @Column(name = "price")
    private Map<Integer, BigDecimal> priceOptions;

    // Multi-Location Choice: [ONLINE, TUTOR_HOME, STUDENT_HOME]
    @ElementCollection(targetClass = LearningMode.class)
    @Enumerated(EnumType.STRING)
    private Set<LearningMode> learningModes;

    private String city;
    private String district;
    private String address;

    @OneToMany(mappedBy = "openClass", cascade = CascadeType.ALL)
    private List<ClassSchedule> schedules;

    public enum LearningMode { STUDENT_HOME, TUTOR_HOME, ONLINE, OUTSIDE }
}