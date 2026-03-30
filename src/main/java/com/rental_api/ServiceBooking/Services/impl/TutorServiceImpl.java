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

        Tutor tutor = tutorRepository.findByUserEmail(email)
            .orElseGet(() -> {
                User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UserNotFoundException("User not found: " + email));
                return Tutor.builder().user(user).build();
            });

        // 1. Update Profile Image (Cloudinary)
        if (profileImg != null && !profileImg.isEmpty()) {
            if (tutor.getProfilePicture() != null) {
                cloudinaryService.deleteFile(tutor.getProfilePicture());
            }
            tutor.setProfilePicture(cloudinaryService.uploadFile(profileImg));
        }

        // 2. Manage Media Table (Video & Certs)
        TutorMedia media = tutor.getMedia();
        if (media == null) {
            media = TutorMedia.builder()
                         .tutor(tutor)
                         .certificateImages(new ArrayList<>())
                         .mediaType("VIDEO") 
                         .build();
            tutor.setMedia(media);
        }

        // Upload Video
        if (videoFile != null && !videoFile.isEmpty()) {
            if (media.getIntroVideoUrl() != null) {
                cloudinaryService.deleteFile(media.getIntroVideoUrl());
            }
            String videoUrl = cloudinaryService.uploadFile(videoFile);
            media.setIntroVideoUrl(videoUrl);
            media.setUrl(videoUrl); 
        }

        // Upload Multiple Certificates
        if (certs != null && !certs.isEmpty()) {
            List<String> uploadedCerts = certs.stream()
                    .filter(file -> file != null && !file.isEmpty())
                    .map(cloudinaryService::uploadFile)
                    .collect(Collectors.toList());
            media.getCertificateImages().addAll(uploadedCerts);
        }

        // 3. Update Text Metadata
        tutor.setBio(request.getBio());

        // Refresh Education
        tutor.getEducation().clear();
        if (request.getEducation() != null) {
            request.getEducation().forEach(edu -> {
                tutor.getEducation().add(Education.builder()
                    .schoolName(edu.getSchool())
                    .degree(edu.getDegree())
                    .yearFinished(edu.getYear())
                    .tutor(tutor).build());
            });
        }

        // Refresh Experience
        tutor.getExperience().clear();
        if (request.getExperience() != null) {
            request.getExperience().forEach(exp -> {
                tutor.getExperience().add(Experience.builder()
                    .companyName(exp.getCompany())
                    .role(exp.getRole())
                    .duration(exp.getDuration())
                    .tutor(tutor).build());
            });
        }

        tutorRepository.save(tutor);
        log.info("Cloud-synced update complete for tutor: {}", email);
    }

    @Override
    @Transactional(readOnly = true)
    public TutorFullViewResponse getMyOwnProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Tutor tutor = tutorRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Tutor profile not found for " + email));
        return mapToResponse(tutor);
    }

    @Override
    @Transactional(readOnly = true)
    public TutorFullViewResponse getTutorFullDetail(Long tutorId) {
        Tutor tutor = tutorRepository.findById(tutorId)
                .orElseThrow(() -> new ResourceNotFoundException("Tutor not found with ID: " + tutorId));
        return mapToResponse(tutor);
    }

    @Override
    @Transactional
    public void publishProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Tutor tutor = tutorRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));
        
        if (tutor.getProfilePicture() == null || tutor.getBio() == null) {
            throw new RuntimeException("Profile incomplete: Bio and Picture required to publish.");
        }
        tutor.setPublic(true);
        tutorRepository.save(tutor);
    }

    @Override
    @Transactional
    public void unpublishProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Tutor tutor = tutorRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));
        tutor.setPublic(false);
        tutorRepository.save(tutor);
    }

    @Override
    @Transactional
    public void incrementStudentCount(Long tutorId) {
        tutorRepository.findById(tutorId).ifPresent(t -> {
            t.setTotalStudentsTaught(t.getTotalStudentsTaught() + 1);
            tutorRepository.save(t);
        });
    }

    // --- Private Helper ---
    private TutorFullViewResponse mapToResponse(Tutor tutor) {
        return TutorFullViewResponse.builder()
            .tutorId(tutor.getId())
            .fullname(tutor.getUser().getFullname())
            .bio(tutor.getBio())
            .profilePicture(tutor.getProfilePicture())
            .introVideoUrl(tutor.getMedia() != null ? tutor.getMedia().getIntroVideoUrl() : null)
            .certificateImages(tutor.getMedia() != null ? tutor.getMedia().getCertificateImages() : new ArrayList<>())
            .isPublic(tutor.isPublic())
            .rating(tutor.getAverageRating())
            .studentsTaught(tutor.getTotalStudentsTaught())
            .education(tutor.getEducation().stream()
                .map(e -> new TutorFullViewResponse.EducationDto(e.getSchoolName(), e.getDegree(), e.getYearFinished()))
                .collect(Collectors.toList()))
            .experience(tutor.getExperience().stream()
                .map(ex -> new TutorFullViewResponse.ExperienceDto(ex.getCompanyName(), ex.getRole(), ex.getDuration()))
                .collect(Collectors.toList()))
            .build();
    }
}