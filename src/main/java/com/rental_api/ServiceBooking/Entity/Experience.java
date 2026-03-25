package com.rental_api.ServiceBooking.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tutor_experiences")
@Getter 
@Setter 
@Builder 
@NoArgsConstructor 
@AllArgsConstructor
public class Experience {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String companyName; // e.g., "Northbridge International School"

    @Column(nullable = false)
    private String role; // e.g., "Senior Mathematics Teacher"

    private String duration; // e.g., "2019 - 2024" or "5 Years"

    private String description; // Optional: "Taught Grade 12 Calculus"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_id")
    private Tutor tutor;
}