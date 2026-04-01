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

    private String profileImageUrl; // optional profile image
    private String introVideoUrl;   // optional video
    private String coverImageUrl;   // optional cover image

    @Builder.Default
    @ElementCollection
    @CollectionTable(name = "tutor_certificates", joinColumns = @JoinColumn(name = "media_id"))
    @Column(name = "certificate_url")
    private List<String> certificateImages = new ArrayList<>();
}