package com.rental_api.ServiceBooking.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tutor_educations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Education {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String schoolName; // e.g., "Royal University of Phnom Penh"

    @Column(nullable = false)
    private String degree; // e.g., "Bachelor of Computer Science"

    @Column(length = 4)
    private String yearFinished; // e.g., "2022"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_id")
    private Tutor tutor;
}