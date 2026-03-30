package com.rental_api.ServiceBooking.Services.impl;

import com.rental_api.ServiceBooking.Dto.Request.TutorProfileRequest;
import com.rental_api.ServiceBooking.Dto.Response.TutorFullViewResponse;
import com.rental_api.ServiceBooking.Entity.*;
import com.rental_api.ServiceBooking.Exception.ResourceNotFoundException;
import com.rental_api.ServiceBooking.Exception.UserNotFoundException;
import com.rental_api.ServiceBooking.Repository.TutorRepository;
import com.rental_api.ServiceBooking.Repository.UserRepository;
import com.rental_api.ServiceBooking.Services.CloudinaryService; 
import com.rental_api.ServiceBooking.Services.TutorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TutorServiceImpl implements TutorService {

    private final TutorRepository tutorRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService; 

    @Override
    @Transactional
    public void updateTutorProfile(TutorProfileRequest request, 
                                   MultipartFile profileImg, 
                                   MultipartFile videoFile, 
                                   List<MultipartFile> certs) {
        
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Updating profile for: {}", email);

        Tutor tutor = tutorRepository.findByUserEmail(email)
            .orElseGet(() -> {
                User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UserNotFoundException("User not found: " + email));
                return Tutor.builder().user(user).build();
            });

        // 1. Upload Profile Image to Cloudinary
        if (profileImg != null && !profileImg.isEmpty()) {
            // Delete old image from Cloud if it exists
            if (tutor.getProfilePicture() != null) {
                cloudinaryService.deleteFile(tutor.getProfilePicture());
            }
            tutor.setProfilePicture(cloudinaryService.uploadFile(profileImg));
        }

        // 2. Set Bio
        if (request.getBio() != null) {
            tutor.setBio(request.getBio());
        }

        // 3. Handle Media (Video/Certificates)
        TutorMedia media = tutor.getMedia();
        if (media == null) {
            media = TutorMedia.builder()
                         .tutor(tutor)
                         .certificateImages(new ArrayList<>())
                         .build();
            tutor.setMedia(media);
        }

        if (videoFile != null && !videoFile.isEmpty()) {
            media.setIntroVideoUrl(cloudinaryService.uploadFile(videoFile));
        }

        if (certs != null && !certs.isEmpty()) {
            List<String> urls = certs.stream()
                    .filter(f -> f != null && !f.isEmpty())
                    .map(cloudinaryService::uploadFile)
                    .collect(Collectors.toList());
            media.getCertificateImages().addAll(urls);
        }

        // 4. Refresh Collections (Education/Experience)
        refreshCollections(tutor, request);

        // 🔥 THE FIX: Commit to DB immediately so publishProfile() can see the data
        tutorRepository.saveAndFlush(tutor);
        log.info("Profile synchronized and flushed for tutor: {}", email);
    }

    @Override
    @Transactional
    public void publishProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Tutor tutor = tutorRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Tutor profile not found"));

        // Validation - Ensures data exists before making it public
        if (tutor.getProfilePicture() == null || tutor.getBio() == null || tutor.getBio().isBlank()) {
            log.warn("Validation failed for {}: Bio={} Pic={}", email, tutor.getBio(), tutor.getProfilePicture());
            throw new IllegalArgumentException("Cannot publish: Profile picture and bio are required.");
        }

        tutor.setPublic(true);
        tutorRepository.save(tutor);
        log.info("Tutor {} is now public.", email);
    }

    @Override
    @Transactional(readOnly = true)
    public TutorFullViewResponse getMyOwnProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Tutor tutor = tutorRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));
        return mapToResponse(tutor);
    }

    @Override
    @Transactional(readOnly = true)
    public TutorFullViewResponse getTutorFullDetail(Long tutorId) {
        Tutor tutor = tutorRepository.findById(tutorId)
                .orElseThrow(() -> new ResourceNotFoundException("Tutor not found"));
        return mapToResponse(tutor);
    }

    @Override
    @Transactional
    public void unpublishProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        tutorRepository.findByUserEmail(email).ifPresent(t -> {
            t.setPublic(false);
            tutorRepository.save(t);
        });
    }

    @Override
    @Transactional
    public void incrementStudentCount(Long tutorId) {
        tutorRepository.findById(tutorId).ifPresent(t -> {
            t.setTotalStudentsTaught(t.getTotalStudentsTaught() + 1);
            tutorRepository.save(t);
        });
    }

    // --- Private Helpers ---

    private void refreshCollections(Tutor tutor, TutorProfileRequest request) {
        tutor.getEducation().clear();
        if (request.getEducation() != null) {
            request.getEducation().forEach(e -> tutor.getEducation().add(
                Education.builder().schoolName(e.getSchool()).degree(e.getDegree())
                        .yearFinished(e.getYear()).tutor(tutor).build()
            ));
        }
        tutor.getExperience().clear();
        if (request.getExperience() != null) {
            request.getExperience().forEach(ex -> tutor.getExperience().add(
                Experience.builder().companyName(ex.getCompany()).role(ex.getRole())
                        .duration(ex.getDuration()).tutor(tutor).build()
            ));
        }
    }

    private TutorFullViewResponse mapToResponse(Tutor t) {
        return TutorFullViewResponse.builder()
            .tutorId(t.getId())
            .fullname(t.getUser().getFullname())
            .bio(t.getBio())
            .profilePicture(t.getProfilePicture())
            .isPublic(t.isPublic())
            .rating(t.getAverageRating())
            .studentsTaught(t.getTotalStudentsTaught())
            .education(t.getEducation().stream()
                .map(e -> new TutorFullViewResponse.EducationDto(e.getSchoolName(), e.getDegree(), e.getYearFinished()))
                .collect(Collectors.toList()))
            .experience(t.getExperience().stream()
                .map(ex -> new TutorFullViewResponse.ExperienceDto(ex.getCompanyName(), ex.getRole(), ex.getDuration()))
                .collect(Collectors.toList()))
            .build();
    }
}