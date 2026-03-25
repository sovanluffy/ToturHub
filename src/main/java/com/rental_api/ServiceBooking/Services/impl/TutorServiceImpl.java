package com.rental_api.ServiceBooking.Services.impl;

import com.rental_api.ServiceBooking.Dto.Request.TutorRequestDto;
import com.rental_api.ServiceBooking.Dto.Response.TutorResponseDto;
import com.rental_api.ServiceBooking.Entity.Subject;
import com.rental_api.ServiceBooking.Entity.Tutor;
import com.rental_api.ServiceBooking.Entity.User;
import com.rental_api.ServiceBooking.Repository.SubjectRepository;
import com.rental_api.ServiceBooking.Repository.TutorRepository;
import com.rental_api.ServiceBooking.Repository.UserRepository;
import com.rental_api.ServiceBooking.Services.TutorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TutorServiceImpl implements TutorService {

    private final TutorRepository tutorRepository;
    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;

    @Override
    @Transactional(readOnly = true)
    public List<TutorResponseDto> getAllTutors() {
        return tutorRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TutorResponseDto getTutorById(Long id) {
        Tutor tutor = tutorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tutor not found with id: " + id));
        return mapToResponse(tutor);
    }

    @Override
    @Transactional
    public TutorResponseDto createTutor(TutorRequestDto request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Fetch subjects based on IDs sent in request
        List<Subject> subjects = subjectRepository.findAllById(request.getSubjectIds());

        Tutor tutor = Tutor.builder()
                .user(user)
                .bio(request.getBio())
                .experienceYears(request.getExperienceYears())
                .pricePerHour(request.getPricePerHour())
                .telegram(request.getTelegram())
                .subjects(subjects)
                .build();

        return mapToResponse(tutorRepository.save(tutor));
    }

    @Override
    @Transactional
    public TutorResponseDto updateTutor(Long id, TutorRequestDto request) {
        Tutor tutor = tutorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tutor profile not found"));
        
        tutor.setBio(request.getBio());
        tutor.setPricePerHour(request.getPricePerHour());
        tutor.setExperienceYears(request.getExperienceYears());
        
        if (request.getSubjectIds() != null) {
            List<Subject> subjects = subjectRepository.findAllById(request.getSubjectIds());
            tutor.setSubjects(subjects);
        }

        return mapToResponse(tutorRepository.save(tutor));
    }

    @Override
    @Transactional
    public void deleteTutor(Long id) {
        tutorRepository.deleteById(id);
    }

    // --- Helper: Entity to Response DTO Mapping ---
    private TutorResponseDto mapToResponse(Tutor tutor) {
        return TutorResponseDto.builder()
                .id(tutor.getId())
                .fullname(tutor.getUser().getFullname())
                .email(tutor.getUser().getEmail())
                .avatarUrl(tutor.getUser().getAvatarUrl())
                .bio(tutor.getBio())
                .experienceYears(tutor.getExperienceYears())
                .pricePerHour(tutor.getPricePerHour())
                .telegram(tutor.getTelegram())
                .subjects(tutor.getSubjects().stream()
                        .map(Subject::getName)
                        .collect(Collectors.toList()))
                .build();
    }
}