package com.rental_api.ServiceBooking.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "reviews")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String comment;
    private Integer rating; // e.g., 1 to 5 stars

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_id")
    private Tutor tutor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private User student; // The user who wrote the review
}