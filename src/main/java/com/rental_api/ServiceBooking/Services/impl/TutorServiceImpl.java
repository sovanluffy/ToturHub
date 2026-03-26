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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TutorServiceImpl implements TutorService {

    private final TutorRepository tutorRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService; 

    /**
     * ✅ NEW: PUBLISH PROFILE LOGIC
     * This flips the 'isPublic' switch so the tutor card appears on the homepage.
     */
    @Override
    @Transactional
    public void publishProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Tutor tutor = tutorRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Tutor profile not found. Please save your info first."));
        
        // Optional: Add a check to ensure profile is complete before publishing
        if (tutor.getProfilePicture() == null || tutor.getBio() == null) {
            throw new RuntimeException("Cannot publish: Profile picture and bio are required.");
        }

        tutor.setPublic(true); // Assuming you added 'private boolean isPublic' to Tutor entity
        tutorRepository.save(tutor);
    }

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
                    .orElseThrow(() -> new UserNotFoundException("User not found"));
                return Tutor.builder().user(user).build();
            });

        // 1. Handle Profile Image
        if (profileImg != null && !profileImg.isEmpty()) {
            tutor.setProfilePicture(cloudinaryService.uploadFile(profileImg));
        }

        // 2. Handle Media & DB Constraints
        TutorMedia media = tutor.getMedia();
        if (media == null) {
            media = TutorMedia.builder()
                         .tutor(tutor)
                         .mediaType("VIDEO") 
                         .url("PENDING") 
                         .build();
            tutor.setMedia(media);
        }

        if (videoFile != null && !videoFile.isEmpty()) {
            String videoPath = cloudinaryService.uploadFile(videoFile);
            media.setIntroVideoUrl(videoPath);
            media.setUrl(videoPath); 
        }

        if (certs != null && !certs.isEmpty()) {
            List<String> uploadedCerts = certs.stream()
                    .filter(file -> file != null && !file.isEmpty())
                    .map(cloudinaryService::uploadFile)
                    .collect(Collectors.toList());
            media.getCertificateImages().addAll(uploadedCerts);
        }

        // 3. Update Text Content
        tutor.setBio(request.getBio());

        tutor.getEducation().clear();
        if (request.getEducation() != null) {
            request.getEducation().forEach(eduReq -> {
                tutor.getEducation().add(Education.builder()
                    .schoolName(eduReq.getSchool())
                    .degree(eduReq.getDegree())
                    .yearFinished(eduReq.getYear())
                    .tutor(tutor).build());
            });
        }

        tutor.getExperience().clear();
        if (request.getExperience() != null) {
            request.getExperience().forEach(expReq -> {
                tutor.getExperience().add(Experience.builder()
                    .companyName(expReq.getCompany())
                    .role(expReq.getRole())
                    .duration(expReq.getDuration())
                    .tutor(tutor).build());
            });
        }

        // Note: isPublic stays false here until they click 'Publish'
        tutorRepository.save(tutor);
    }

    @Override
    @Transactional(readOnly = true)
    public TutorFullViewResponse getTutorFullDetail(Long tutorId) {
        Tutor tutor = tutorRepository.findById(tutorId)
                .orElseThrow(() -> new ResourceNotFoundException("Tutor not found"));

        return TutorFullViewResponse.builder()
                .tutorId(tutor.getId())
                .fullname(tutor.getUser().getFullname()) 
                .bio(tutor.getBio())
                .profilePicture(tutor.getProfilePicture())
                .introVideoUrl(tutor.getMedia() != null ? tutor.getMedia().getIntroVideoUrl() : null)
                .certificateImages(tutor.getMedia() != null ? tutor.getMedia().getCertificateImages() : null)
                .isPublic(tutor.isPublic()) // Added to response so frontend knows the status
                .education(tutor.getEducation().stream()
                    .map(e -> new TutorFullViewResponse.EducationDto(e.getSchoolName(), e.getDegree(), e.getYearFinished()))
                    .collect(Collectors.toList()))
                .experience(tutor.getExperience().stream()
                    .map(ex -> new TutorFullViewResponse.ExperienceDto(ex.getCompanyName(), ex.getRole(), ex.getDuration()))
                    .collect(Collectors.toList()))
                .build();
    }

    @Override
    @Transactional
    public void incrementStudentCount(Long tutorId) {
        tutorRepository.findById(tutorId).ifPresent(t -> {
            t.setTotalStudentsTaught(t.getTotalStudentsTaught() + 1);
            tutorRepository.save(t);
        });
    }


    @Override
@Transactional
public void unpublishProfile() {
    // 1. Get the current logged-in user's email
    String email = SecurityContextHolder.getContext().getAuthentication().getName();

    // 2. Find the tutor profile
    Tutor tutor = tutorRepository.findByUserEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Tutor profile not found"));

    // 3. Set to false so they disappear from the public website
    tutor.setPublic(false); 
    
    tutorRepository.save(tutor);
}


@Override
@Transactional(readOnly = true)
public TutorFullViewResponse getMyOwnProfile() {
    // 1. Get email from Token
    String email = SecurityContextHolder.getContext().getAuthentication().getName();

    // 2. Find the tutor linked to this email
    Tutor tutor = tutorRepository.findByUserEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Profile not found. Please create one first."));

    // 3. Map to Full View (reuse your existing mapping logic)
    return TutorFullViewResponse.builder()
            .tutorId(tutor.getId())
            .fullname(tutor.getUser().getFullname())
            .bio(tutor.getBio())
            .profilePicture(tutor.getProfilePicture())
            .isPublic(tutor.isPublic()) // 👈 Very important for the Tutor to see!
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