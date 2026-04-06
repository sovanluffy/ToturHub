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

    @Override
    @Transactional
    public void updateTutorProfile(TutorProfileRequest request,
                                   MultipartFile profileImg,
                                   MultipartFile videoFile,
                                   MultipartFile coverImage,
                                   List<MultipartFile> certificates) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Updating tutor profile for {}", email);

        User user = validateUserAndRole(email);
        validateRequest(request);

        Tutor tutor = tutorRepository.findByUserEmail(email)
                .orElseGet(() -> Tutor.builder().user(user).build());

        // Initialize Media if null
        TutorMedia media = tutor.getMedia();
        if (media == null) {
            media = TutorMedia.builder()
                    .tutor(tutor)
                    .mediaType("IMAGE") // Default value
                    .certificateImages(new ArrayList<>())
                    .build();
            tutor.setMedia(media);
        }

        try {
            boolean hasVideo = (media.getIntroVideoUrl() != null);
            boolean hasImages = (media.getProfileImageUrl() != null || media.getCoverImageUrl() != null);

            if (profileImg != null && !profileImg.isEmpty()) {
                media.setProfileImageUrl(cloudinaryService.uploadFile(profileImg));
                hasImages = true;
            }

            if (videoFile != null && !videoFile.isEmpty()) {
                media.setIntroVideoUrl(cloudinaryService.uploadFile(videoFile));
                hasVideo = true;
            }

            if (coverImage != null && !coverImage.isEmpty()) {
                media.setCoverImageUrl(cloudinaryService.uploadFile(coverImage));
                hasImages = true;
            }

            if (certificates != null && !certificates.isEmpty()) {
                List<String> urls = certificates.stream()
                        .filter(f -> f != null && !f.isEmpty())
                        .map(cloudinaryService::uploadFile)
                        .toList();
                media.getCertificateImages().addAll(urls);
                hasImages = true;
            }

            // Correctly determine Media Type
            if (hasVideo && hasImages) {
                media.setMediaType("MIXED");
            } else if (hasVideo) {
                media.setMediaType("VIDEO");
            } else {
                media.setMediaType("IMAGE");
            }

        } catch (Exception e) {
            log.error("Cloudinary upload failed", e);
            throw new RuntimeException("File upload failed: " + e.getMessage());
        }

        tutor.setBio(request.getBio());
        refreshCollections(tutor, request);

        tutorRepository.saveAndFlush(tutor);
    }

    private User validateUserAndRole(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + email));

        boolean isTutor = user.getRoles().stream()
                .anyMatch(role -> role.getName().equalsIgnoreCase("TUTOR"));

        if (!isTutor) {
            throw new IllegalArgumentException("Access denied: User is not a tutor");
        }
        return user;
    }

    private void validateRequest(TutorProfileRequest request) {
        if (request.getBio() == null || request.getBio().isBlank()) {
            throw new IllegalArgumentException("Bio is required");
        }
    }

    @Override
    @Transactional
    public void publishProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Tutor tutor = tutorRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Tutor profile not found"));
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

    @Override
    @Transactional
    public void adminUnpublishTutor(Long tutorId) {
        Tutor tutor = tutorRepository.findById(tutorId)
                .orElseThrow(() -> new ResourceNotFoundException("Tutor not found"));
        tutor.setPublic(false);
        tutorRepository.save(tutor);
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
    public void incrementStudentCount(Long tutorId) {
        tutorRepository.findById(tutorId).ifPresent(tutor -> {
            tutor.setTotalStudentsTaught(tutor.getTotalStudentsTaught() + 1);
            tutorRepository.save(tutor);
        });
    }

    private void refreshCollections(Tutor tutor, TutorProfileRequest request) {
        if (tutor.getEducation() == null) tutor.setEducation(new ArrayList<>());
        tutor.getEducation().clear();
        if (request.getEducation() != null) {
            request.getEducation().forEach(e ->
                    tutor.getEducation().add(Education.builder()
                            .schoolName(e.getSchool()).degree(e.getDegree())
                            .yearFinished(e.getYear()).tutor(tutor).build())
            );
        }

        if (tutor.getExperience() == null) tutor.setExperience(new ArrayList<>());
        tutor.getExperience().clear();
        if (request.getExperience() != null) {
            request.getExperience().forEach(ex ->
                    tutor.getExperience().add(Experience.builder()
                            .companyName(ex.getCompany()).role(ex.getRole())
                            .duration(ex.getDuration()).tutor(tutor).build())
            );
        }
    }

    private TutorFullViewResponse mapToResponse(Tutor t) {
        TutorMedia media = t.getMedia();
        String profilePic = (media != null && media.getProfileImageUrl() != null)
                ? media.getProfileImageUrl() : t.getUser().getAvatarUrl();

        return TutorFullViewResponse.builder()
                .tutorId(t.getId())
                .fullname(t.getUser().getFullname())
                .bio(t.getBio())
                .profilePicture(profilePic)
                .introVideoUrl(media != null ? media.getIntroVideoUrl() : null)
                .certificateImages(media != null ? media.getCertificateImages() : new ArrayList<>())
                .rating(t.getAverageRating())
                .studentsTaught(t.getTotalStudentsTaught())
                .isPublic(t.isPublic())
                .education(t.getEducation().stream()
                        .map(e -> new TutorFullViewResponse.EducationDto(e.getSchoolName(), e.getDegree(), e.getYearFinished()))
                        .toList())
                .experience(t.getExperience().stream()
                        .map(ex -> new TutorFullViewResponse.ExperienceDto(ex.getCompanyName(), ex.getRole(), ex.getDuration()))
                        .toList())
                .activeClasses(new ArrayList<>())
                .reviews(new ArrayList<>())
                .build();
    }
}