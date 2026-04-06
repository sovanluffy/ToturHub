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

    // ------------------- UPDATE PROFILE -------------------
    @Override
    @Transactional
    public void updateTutorProfile(TutorProfileRequest request,
                                   MultipartFile profileImg,
                                   MultipartFile videoFile,
                                   MultipartFile coverImage,
                                   List<MultipartFile> certificates) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = validateUserAndRole(email);

        // Get existing tutor or create one if this is their first update
        Tutor tutor = tutorRepository.findByUserEmail(email)
                .orElseGet(() -> tutorRepository.save(Tutor.builder().user(user).build()));

        // Initialize Media if null
        TutorMedia media = tutor.getMedia();
        if (media == null) {
            media = TutorMedia.builder()
                    .tutor(tutor)
                    .certificateImages(new ArrayList<>())
                    .build();
            tutor.setMedia(media);
        }

        try {
            // ✅ IMAGE SYNC: Save to both TutorMedia and User Table
            if (profileImg != null && !profileImg.isEmpty()) {
                String uploadedUrl = cloudinaryService.uploadFile(profileImg);
                media.setProfileImageUrl(uploadedUrl);
                
                // Update User avatar so it's consistent across the app
                user.setAvatarUrl(uploadedUrl);
                userRepository.save(user);
            }

            if (videoFile != null && !videoFile.isEmpty()) {
                media.setIntroVideoUrl(cloudinaryService.uploadFile(videoFile));
            }

            if (coverImage != null && !coverImage.isEmpty()) {
                media.setCoverImageUrl(cloudinaryService.uploadFile(coverImage));
            }

            if (certificates != null && !certificates.isEmpty()) {
                List<String> urls = certificates.stream()
                        .filter(f -> f != null && !f.isEmpty())
                        .map(cloudinaryService::uploadFile)
                        .toList();
                media.getCertificateImages().addAll(urls);
            }

            updateMediaType(media);

        } catch (Exception e) {
            log.error("Cloudinary upload failed", e);
            throw new RuntimeException("File upload failed: " + e.getMessage());
        }

        tutor.setBio(request.getBio());
        refreshCollections(tutor, request);
        
        // Save and Flush ensures immediate DB sync
        tutorRepository.saveAndFlush(tutor);
    }

    // ------------------- PUBLISH / UNPUBLISH -------------------
    @Override
    @Transactional
    public void publishProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Tutor tutor = tutorRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Tutor profile not found"));
        
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
        tutorRepository.findByUserEmail(email).ifPresent(tutor -> {
            tutor.setPublic(false);
            tutorRepository.save(tutor);
        });
    }

    // ------------------- PROFILE FETCHING -------------------
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

    // ------------------- HELPERS -------------------
    private TutorFullViewResponse mapToResponse(Tutor t) {
        TutorMedia media = t.getMedia();
        
        // ✅ PRIORITY CHECK: Use TutorMedia image, fallback to User avatar
        String profilePic = (media != null && media.getProfileImageUrl() != null) 
                            ? media.getProfileImageUrl() 
                            : t.getUser().getAvatarUrl();

        return TutorFullViewResponse.builder()
                .tutorId(t.getId())
                .fullname(t.getUser().getFullname())
                .profilePicture(profilePic)
                .bio(t.getBio())
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

    private User validateUserAndRole(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        
        boolean isTutor = user.getRoles().stream()
                .anyMatch(r -> r.getName().equalsIgnoreCase("ROLE_TUTOR") || r.getName().equalsIgnoreCase("tutor"));
        
        if (!isTutor) throw new IllegalArgumentException("Access denied: User is not a tutor");
        return user;
    }

    private void updateMediaType(TutorMedia media) {
        boolean hasVideo = (media.getIntroVideoUrl() != null);
        boolean hasImages = (media.getProfileImageUrl() != null || media.getCoverImageUrl() != null);
        if (hasVideo && hasImages) media.setMediaType("MIXED");
        else if (hasVideo) media.setMediaType("VIDEO");
        else media.setMediaType("IMAGE");
    }

    private void refreshCollections(Tutor tutor, TutorProfileRequest request) {
        if (tutor.getEducation() == null) tutor.setEducation(new ArrayList<>());
        tutor.getEducation().clear();
        if (request.getEducation() != null) {
            request.getEducation().forEach(e -> tutor.getEducation().add(
                Education.builder().schoolName(e.getSchool()).degree(e.getDegree()).yearFinished(e.getYear()).tutor(tutor).build()
            ));
        }

        if (tutor.getExperience() == null) tutor.setExperience(new ArrayList<>());
        tutor.getExperience().clear();
        if (request.getExperience() != null) {
            request.getExperience().forEach(ex -> tutor.getExperience().add(
                Experience.builder().companyName(ex.getCompany()).role(ex.getRole()).duration(ex.getDuration()).tutor(tutor).build()
            ));
        }
    }

    @Override
    @Transactional
    public void adminUnpublishTutor(Long tutorId) {
        Tutor tutor = tutorRepository.findById(tutorId).orElseThrow();
        tutor.setPublic(false);
        tutorRepository.save(tutor);
    }

    @Override
    @Transactional
    public void incrementStudentCount(Long tutorId) {
        tutorRepository.findById(tutorId).ifPresent(t -> t.setTotalStudentsTaught(t.getTotalStudentsTaught() + 1));
    }
}