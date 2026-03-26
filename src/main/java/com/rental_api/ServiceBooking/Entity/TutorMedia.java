package com.rental_api.ServiceBooking.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tutor_media")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class TutorMedia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "tutor_id")
    private Tutor tutor;

    // This is the field you currently have
    private String introVideoUrl;

    // ✅ ADD THIS FIELD to fix the "column 'url' violates not-null" error
    @Builder.Default
    @Column(name = "url", nullable = false)
    private String url = "PENDING"; // Default value to satisfy the DB constraint

    @Builder.Default
    @Column(name = "media_type", nullable = false)
    private String mediaType = "VIDEO"; 

    @Builder.Default
    @ElementCollection
    @CollectionTable(name = "tutor_certificates", joinColumns = @JoinColumn(name = "media_id"))
    @Column(name = "certificate_url")
    private List<String> certificateImages = new ArrayList<>();
}