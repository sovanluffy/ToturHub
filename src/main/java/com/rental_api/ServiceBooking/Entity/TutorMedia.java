package com.rental_api.ServiceBooking.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tutor_media")
@Getter 
@Setter 
@Builder 
@NoArgsConstructor 
@AllArgsConstructor
public class TutorMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "tutor_id", nullable = false)
    private Tutor tutor;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Column(name = "intro_video_url")
    private String introVideoUrl;

    @Column(name = "cover_image_url")
    private String coverImageUrl;

    @Column(name = "media_type", nullable = false)
    private String mediaType; // IMAGE, VIDEO, MIXED

    @Builder.Default
    @ElementCollection
    @CollectionTable(
        name = "tutor_certificates", 
        joinColumns = @JoinColumn(name = "media_id")
    )
    @Column(name = "certificate_url")
    private List<String> certificateImages = new ArrayList<>();
}