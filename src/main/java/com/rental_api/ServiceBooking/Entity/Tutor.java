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
    @JoinColumn(name = "user_id")
    private User user;

    private String bio;
    private String profilePicture; 

    // ✅ ADD THIS FIELD TO RESOLVE THE ERROR
    @Builder.Default
    @Column(name = "years_of_experience")
    private Integer yearsOfExperience = 0; 

    // ✅ Relationship to the new Media Entity
    @OneToOne(mappedBy = "tutor", cascade = CascadeType.ALL, orphanRemoval = true)
    private TutorMedia media;

    // --- Professional History Timelines ---
    @Builder.Default
    @OneToMany(mappedBy = "tutor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Education> education = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "tutor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Experience> experience = new ArrayList<>();

    // --- "Post More" Logic ---
    @Builder.Default
    @OneToMany(mappedBy = "tutor", cascade = CascadeType.ALL)
    private List<OpenClass> openClasses = new ArrayList<>();

    // --- Global Stats ---
    @Builder.Default
    private Double averageRating = 0.0;
    
    @Builder.Default
    private Integer totalStudentsTaught = 0;

    @Builder.Default
    @ManyToMany
    @JoinTable(
        name = "tutor_subjects",
        joinColumns = @JoinColumn(name = "tutor_id"),
        inverseJoinColumns = @JoinColumn(name = "subject_id")
    )
    private List<Subject> subjects = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "tutor")
    private List<Review> reviews = new ArrayList<>();

    @Column(name = "is_public", nullable = false)
    private boolean isPublic = false; 
}