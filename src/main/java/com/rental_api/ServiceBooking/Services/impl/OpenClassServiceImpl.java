package com.rental_api.ServiceBooking.Services.impl;

import com.rental_api.ServiceBooking.Dto.Request.OpenClassRequest;
import com.rental_api.ServiceBooking.Dto.Response.OpenClassResponse;
import com.rental_api.ServiceBooking.Dto.Response.TutorCardResponse;
import com.rental_api.ServiceBooking.Entity.*;
import com.rental_api.ServiceBooking.Entity.OpenClass.ClassStatus;
import com.rental_api.ServiceBooking.Entity.OpenClass.LearningMode;
import com.rental_api.ServiceBooking.Repository.*;
import com.rental_api.ServiceBooking.Services.OpenClassService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OpenClassServiceImpl implements OpenClassService {

    private final OpenClassRepository openClassRepository;
    private final TutorRepository tutorRepository;
    private final SubjectRepository subjectRepository;

    @Override
@Transactional
public OpenClassResponse createClass(OpenClassRequest request) {
    // 1. Get Tutor from Token
    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    Tutor tutor = tutorRepository.findByUserEmail(email)
            .orElseThrow(() -> new RuntimeException("Tutor not found"));

    // 2. Fetch Subjects
    List<Subject> subjects = subjectRepository.findAllById(request.getSubjectIds());

    // 3. Build the OpenClass Object FIRST
    OpenClass openClass = OpenClass.builder()
            .title(request.getTitle())
            .description(request.getDescription())
            .tutor(tutor)
            .subjects(subjects)
            .priceOptions(request.getPriceOptions())
            .learningModes(request.getLearningModes().stream()
                    .map(LearningMode::valueOf).collect(Collectors.toSet()))
            .status(ClassStatus.OPEN)
            .city(request.getCity())
            .district(request.getDistrict())
            .address(request.getAddress())
            .build();

    // 4. Important: Link Schedules to the Parent (The missing piece!)
    if (request.getTimeSlots() != null) {
        List<ClassSchedule> schedules = request.getTimeSlots().stream()
                .map(slot -> ClassSchedule.builder()
                        .startTime(slot.getStartTime())
                        .endTime(slot.getEndTime())
                        .isBooked(false)
                        .openClass(openClass) // 👈 THIS LINKS THE SCHEDULE TO THE CLASS
                        .build())
                .collect(Collectors.toList());
        openClass.setSchedules(schedules);
    }

    // 5. Save (CascadeType.ALL will save the schedules automatically)
    return mapToResponse(openClassRepository.save(openClass));
}

    @Override
    @Transactional(readOnly = true)
    public List<OpenClassResponse> searchClasses(String city, Long subjectId, BigDecimal maxPrice, Integer minExp) {
        Specification<OpenClass> spec = OpenClassSpecification.getFilteredClasses(city, subjectId, maxPrice, minExp);
        return openClassRepository.findAll(spec).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public OpenClassResponse getClassDetails(Long id) {
        OpenClass entity = openClassRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Class not found"));
        return mapToResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TutorCardResponse> getAllPublicCards() {
        return tutorRepository.findAll().stream()
                .filter(Tutor::isPublic)
                .map(t -> TutorCardResponse.builder()
                        .tutorId(t.getId())
                        .fullname(t.getUser().getFullname())
                        .profilePicture(t.getProfilePicture())
                        .rating(t.getAverageRating())
                        .studentsTaught(t.getTotalStudentsTaught())
                        .isPublic(t.isPublic())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OpenClassResponse> findByTutorId(Long tutorId) {
        return openClassRepository.findByTutorId(tutorId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * ✅ Private Helper: Converts Entity to DTO
     */
    private OpenClassResponse mapToResponse(OpenClass entity) {
        Tutor tutor = entity.getTutor();
        return OpenClassResponse.builder()
                .classId(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .tutorId(tutor.getId())
                .tutorName(tutor.getUser().getFullname())
                .tutorImage(tutor.getProfilePicture())
                .tutorRating(tutor.getAverageRating())
                .yearsOfExperience(tutor.getYearsOfExperience())
                .pricing(entity.getPriceOptions())
                .subjects(entity.getSubjects().stream().map(Subject::getName).collect(Collectors.toList()))
                .learningModes(entity.getLearningModes().stream().map(Enum::name).collect(Collectors.toSet()))
                .status(entity.getStatus().name())
                .location(entity.getDistrict() + ", " + entity.getCity())
                .availableSlots(entity.getSchedules().stream()
                        .filter(s -> !s.isBooked())
                        .map(s -> OpenClassResponse.ScheduleDto.builder()
                                .id(s.getId())
                                .timeRange(s.getStartTime() + " to " + s.getEndTime())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}