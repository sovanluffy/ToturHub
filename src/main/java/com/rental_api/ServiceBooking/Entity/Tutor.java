package com.rental_api.ServiceBooking.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tutors")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Tutor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String bio;
    private String profilePicture; 
    private String introVideoUrl; // Link to intro video
    
    @ElementCollection
    private List<String> certificateImages = new ArrayList<>();

    // --- History Timelines ---
    @OneToMany(mappedBy = "tutor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Education> education = new ArrayList<>();

    @OneToMany(mappedBy = "tutor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Experience> experience = new ArrayList<>();

    // --- "Post More" Logic: One tutor can have many classes ---
    @OneToMany(mappedBy = "tutor", cascade = CascadeType.ALL)
    private List<OpenClass> openClasses = new ArrayList<>();

    // --- Global Stats ---
    private Double averageRating = 0.0;
    private Integer totalStudentsTaught = 0;

    @OneToMany(mappedBy = "tutor")
    private List<Review> reviews = new ArrayList<>();
}