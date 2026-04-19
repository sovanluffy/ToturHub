package com.rental_api.ServiceBooking.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tutors")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tutor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Builder.Default
    @Column(name = "years_of_experience")
    private Integer yearsOfExperience = 0;

    // --- Media (profile image, video, certificates) ---
    // Added CascadeType.ALL so saving Tutor automatically saves TutorMedia
    @OneToOne(mappedBy = "tutor", cascade = CascadeType.ALL, orphanRemoval = true)
    private TutorMedia media;

    // --- Education history ---
    @Builder.Default
    @OneToMany(mappedBy = "tutor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Education> education = new ArrayList<>();

    // --- Experience history ---
    @Builder.Default
    @OneToMany(mappedBy = "tutor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Experience> experience = new ArrayList<>();

    // --- Open classes ---
    @Builder.Default
    @OneToMany(mappedBy = "tutor", cascade = CascadeType.ALL)
    private List<OpenClass> openClasses = new ArrayList<>();

    // --- Stats ---
    @Builder.Default
    private Double averageRating = 0.0;

    @Builder.Default
    private Integer totalStudentsTaught = 0;

    // --- Subjects ---
    @Builder.Default
    @ManyToMany
    @JoinTable(name = "tutor_subjects", joinColumns = @JoinColumn(name = "tutor_id"), inverseJoinColumns = @JoinColumn(name = "subject_id"))
    private List<Subject> subjects = new ArrayList<>();

    // --- Reviews ---
    @Builder.Default
    @OneToMany(mappedBy = "tutor")
    private List<Review> reviews = new ArrayList<>();

    @Builder.Default
    @Column(name = "is_public", nullable = false)
    private boolean isPublic = false;
}