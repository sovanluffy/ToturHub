package com.rental_api.ServiceBooking.Services.impl;

import com.rental_api.ServiceBooking.Dto.Request.TutorProfileRequest;
import com.rental_api.ServiceBooking.Dto.Response.TutorFullViewResponse;
import com.rental_api.ServiceBooking.Entity.*;
import com.rental_api.ServiceBooking.Repository.TutorRepository;
import com.rental_api.ServiceBooking.Services.TutorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TutorServiceImpl implements TutorService {

    private final TutorRepository tutorRepository;

    @Override
    @Transactional
    public void updateTutorProfile(TutorProfileRequest request) {
        Tutor tutor = tutorRepository.findById(request.getTutorId())
                .orElseThrow(() -> new RuntimeException("Tutor not found"));

        // 1. Update Media & Basic Info
        tutor.setBio(request.getBio());
        tutor.setProfilePicture(request.getProfilePicture());
        tutor.setIntroVideoUrl(request.getIntroVideoUrl());
        tutor.setCertificateImages(request.getCertificateImages());

        // 2. Update Education Timeline (Clear old and add new)
        tutor.getEducation().clear();
        tutor.getEducation().addAll(request.getEducation().stream()
                .map(edu -> Education.builder()
                        .schoolName(edu.getSchool())
                        .degree(edu.getDegree())
                        .yearFinished(edu.getYear())
                        .tutor(tutor)
                        .build())
                .collect(Collectors.toList()));

        // 3. Update Experience Timeline
        tutor.getExperience().clear();
        tutor.getExperience().addAll(request.getExperience().stream()
                .map(exp -> Experience.builder()
                        .companyName(exp.getCompany())
                        .role(exp.getRole())
                        .duration(exp.getDuration())
                        .tutor(tutor)
                        .build())
                .collect(Collectors.toList()));

        tutorRepository.save(tutor);
    }

    @Override
    @Transactional(readOnly = true)
    public TutorFullViewResponse getTutorFullDetail(Long tutorId) {
        Tutor t = tutorRepository.findById(tutorId)
                .orElseThrow(() -> new RuntimeException("Tutor not found"));

        return TutorFullViewResponse.builder()
                .tutorId(t.getId())
                .fullname(t.getUser().getFullname())
                .bio(t.getBio())
                .profilePicture(t.getProfilePicture())
                .videoUrl(t.getIntroVideoUrl())
                .certificates(t.getCertificateImages())
                .rating(t.getAverageRating())
                .studentsTaught(t.getTotalStudentsTaught())
                
                // Map Education History
                .education(t.getEducation().stream().map(e -> 
                    TutorFullViewResponse.EducationDto.builder()
                        .school(e.getSchoolName()).degree(e.getDegree()).year(e.getYearFinished()).build())
                    .collect(Collectors.toList()))
                
                // Map Experience History
                .experience(t.getExperience().stream().map(ex -> 
                    TutorFullViewResponse.ExperienceDto.builder()
                        .company(ex.getCompanyName()).role(ex.getRole()).duration(ex.getDuration()).build())
                    .collect(Collectors.toList()))

                // THE "POST MORE" LOGIC: Map all active classes posted by this tutor
                .activeClasses(t.getOpenClasses().stream().map(c -> 
                    TutorFullViewResponse.ClassSummaryDto.builder()
                        .id(c.getId())
                        .title(c.getTitle())
                        .prices(c.getPriceOptions())
                        .modes(c.getLearningModes().stream().map(Enum::name).collect(Collectors.toSet()))
                        .build())
                    .collect(Collectors.toList()))

                // Map Social Proof (Reviews)
                .reviews(t.getReviews().stream().map(r -> 
                    TutorFullViewResponse.ReviewDto.builder()
                        .student(r.getStudent().getFullname())
                        .comment(r.getComment())
                        .stars(r.getRating())
                        .build())
                    .collect(Collectors.toList()))
                .build();
    }

    @Override
    @Transactional
    public void incrementStudentCount(Long tutorId) {
        Tutor t = tutorRepository.findById(tutorId).orElseThrow();
        t.setTotalStudentsTaught(t.getTotalStudentsTaught() + 1);
        tutorRepository.save(t);
    }
}