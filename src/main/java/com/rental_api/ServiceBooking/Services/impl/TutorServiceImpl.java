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

@Slf4j
@Service
@RequiredArgsConstructor
public class TutorServiceImpl implements TutorService {

    private final TutorRepository tutorRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;

    // ---------------------------------------------------------
    // 1. UPDATE PROFILE (Syncs Images, Video, and Certificates)
    // ---------------------------------------------------------
    @Override
    @Transactional
    public void updateTutorProfile(TutorProfileRequest request,
                                   MultipartFile profileImg,
                                   MultipartFile videoFile,
                                   MultipartFile coverImg,
                                   List<MultipartFile> certificates) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Role Validation
        boolean isTutor = user.getRoles().stream()
                .anyMatch(r -> r.getName().equalsIgnoreCase("TUTOR") || r.getName().equalsIgnoreCase("ROLE_TUTOR"));
        if (!isTutor) throw new IllegalArgumentException("Access denied: Not a tutor");

        // Fetch or Initialize Tutor Entity
        Tutor tutor = tutorRepository.findByUserEmail(email)
                .orElseGet(() -> tutorRepository.save(Tutor.builder().user(user).build()));

        // Initialize Media and handle mandatory fields
        TutorMedia media = tutor.getMedia();
        if (media == null) {
            media = TutorMedia.builder()
                    .tutor(tutor)
                    .mediaType("MIXED") // Fix: Satisfies @Column(nullable = false)
                    .certificateImages(new ArrayList<>())
                    .build();
            tutor.setMedia(media);
        }

        try {
            // Sync Profile Image to both TutorMedia and User (for global avatar consistency)
            if (profileImg != null && !profileImg.isEmpty()) {
                String uploadedUrl = cloudinaryService.uploadFile(profileImg);
                media.setProfileImageUrl(uploadedUrl);
                user.setAvatarUrl(uploadedUrl);
                userRepository.save(user); 
            }

            // Upload Intro Video
            if (videoFile != null && !videoFile.isEmpty()) {
                media.setIntroVideoUrl(cloudinaryService.uploadFile(videoFile));
            }

            // Upload Cover Image
            if (coverImg != null && !coverImg.isEmpty()) {
                media.setCoverImageUrl(cloudinaryService.uploadFile(coverImg));
            }

            // Upload Multiple Certificates
            if (certificates != null && !certificates.isEmpty()) {
                List<String> urls = certificates.stream()
                        .filter(f -> f != null && !f.isEmpty())
                        .map(cloudinaryService::uploadFile)
                        .toList();
                media.getCertificateImages().addAll(urls);
            }
        } catch (Exception e) {
            log.error("Cloudinary upload failed: ", e);
            throw new RuntimeException("Media upload failed: " + e.getMessage());
        }

        tutor.setBio(request.getBio());
        refreshCollections(tutor, request);
        
        // saveAndFlush ensures the DB is updated before the Controller re-fetches for the response
        tutorRepository.saveAndFlush(tutor);
    }

    // ---------------------------------------------------------
    // 2. FETCHING PROFILES
    // ---------------------------------------------------------
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

    // ---------------------------------------------------------
    // 3. MAPPING (Fixes the 'null' JSON issue)
    // ---------------------------------------------------------
    private TutorFullViewResponse mapToResponse(Tutor t) {
        TutorMedia media = t.getMedia();
        
        // Fallback logic for Profile Picture
        String profilePic = (media != null && media.getProfileImageUrl() != null) 
                            ? media.getProfileImageUrl() 
                            : t.getUser().getAvatarUrl();

        return TutorFullViewResponse.builder()
                .tutorId(t.getId())
                .fullname(t.getUser().getFullname())
                .profilePicture(profilePic)
                .bio(t.getBio())
                // FIXED: Explicitly mapping media fields from Entity to Response DTO
                .introVideoUrl(media != null ? media.getIntroVideoUrl() : null)
                .certificateImages(media != null ? media.getCertificateImages() : new ArrayList<>())
                .rating(t.getAverageRating() != null ? t.getAverageRating() : 0.0)
                .studentsTaught(t.getTotalStudentsTaught() != null ? t.getTotalStudentsTaught() : 0)
                .isPublic(t.isPublic())
                .education(t.getEducation().stream()
                        .map(e -> new TutorFullViewResponse.EducationDto(e.getSchoolName(), e.getDegree(), e.getYearFinished()))
                        .toList())
                .experience(t.getExperience().stream()
                        .map(ex -> new TutorFullViewResponse.ExperienceDto(ex.getCompanyName(), ex.getRole(), ex.getDuration()))
                        .toList())
                .build();
    }

    private void refreshCollections(Tutor tutor, TutorProfileRequest request) {
        // Clear and reload Education
        if (tutor.getEducation() == null) tutor.setEducation(new ArrayList<>());
        tutor.getEducation().clear();
        if (request.getEducation() != null) {
            request.getEducation().forEach(e -> tutor.getEducation().add(
                Education.builder().schoolName(e.getSchool()).degree(e.getDegree()).yearFinished(e.getYear()).tutor(tutor).build()
            ));
        }

        // Clear and reload Experience
        if (tutor.getExperience() == null) tutor.setExperience(new ArrayList<>());
        tutor.getExperience().clear();
        if (request.getExperience() != null) {
            request.getExperience().forEach(ex -> tutor.getExperience().add(
                Experience.builder().companyName(ex.getCompany()).role(ex.getRole()).duration(ex.getDuration()).tutor(tutor).build()
            ));
        }
    }

    // ---------------------------------------------------------
    // 4. PUBLISH / UNPUBLISH / ADMIN
    // ---------------------------------------------------------
    @Override
    @Transactional
    public void publishProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Tutor tutor = tutorRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Tutor profile not found."));
        if (tutor.getBio() == null || tutor.getBio().isBlank()) {
            throw new IllegalArgumentException("Bio is required before publishing.");
        }
        tutor.setPublic(true);
        tutorRepository.save(tutor);
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
    public void adminUnpublishTutor(Long tutorId) {
        tutorRepository.findById(tutorId).ifPresent(t -> {
            t.setPublic(false);
            tutorRepository.save(t);
        });
    }

    @Override
    @Transactional
    public void incrementStudentCount(Long tutorId) {
        tutorRepository.findById(tutorId).ifPresent(t -> {
            int count = (t.getTotalStudentsTaught() != null) ? t.getTotalStudentsTaught() : 0;
            t.setTotalStudentsTaught(count + 1);
        });
    }
}