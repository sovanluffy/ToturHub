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

    @Override
    @Transactional
    public void updateTutorProfile(TutorProfileRequest request, 
                                   MultipartFile profileImg, 
                                   MultipartFile videoFile, 
                                   List<MultipartFile> certs) {
        
        // 1. Identify User from Security Token
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        // 2. Find existing Tutor or create a new entry
        Tutor tutor = tutorRepository.findByUserEmail(email)
            .orElseGet(() -> {
                User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UserNotFoundException("User not found"));
                return Tutor.builder().user(user).build();
            });

        // 3. Handle Profile Image Upload
        if (profileImg != null && !profileImg.isEmpty()) {
            tutor.setProfilePicture(cloudinaryService.uploadFile(profileImg));
        }

        // 4. Initialize Media and fix Database Constraints (media_type and url)
        TutorMedia media = tutor.getMedia();
        if (media == null) {
            media = TutorMedia.builder()
                         .tutor(tutor)
                         .mediaType("VIDEO") 
                         .url("PENDING") // Initial placeholder for the Not Null 'url' column
                         .build();
            tutor.setMedia(media);
        }

        // Handle Video Upload - Fixes the 400 'url' null constraint error
        if (videoFile != null && !videoFile.isEmpty()) {
            String videoPath = cloudinaryService.uploadFile(videoFile);
            media.setIntroVideoUrl(videoPath);
            media.setUrl(videoPath); // ✅ SETTING 'url' TO MATCH 'introVideoUrl'
        }

        // Handle Certificates
        if (certs != null && !certs.isEmpty()) {
            List<String> uploadedCerts = certs.stream()
                    .filter(file -> file != null && !file.isEmpty())
                    .map(cloudinaryService::uploadFile)
                    .collect(Collectors.toList());
            media.getCertificateImages().addAll(uploadedCerts);
        }

        // 5. Update Bio, Education, and Experience
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
}