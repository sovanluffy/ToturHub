package com.rental_api.ServiceBooking.Services.impl;

import com.rental_api.ServiceBooking.Dto.Request.OpenClassRequest;
import com.rental_api.ServiceBooking.Dto.Response.OpenClassResponse;
import com.rental_api.ServiceBooking.Dto.Response.TutorCardResponse;
import com.rental_api.ServiceBooking.Entity.*;
import com.rental_api.ServiceBooking.Entity.OpenClass.LearningMode;
import com.rental_api.ServiceBooking.Entity.OpenClass.ClassStatus;
import com.rental_api.ServiceBooking.Repository.*;
import com.rental_api.ServiceBooking.Services.OpenClassService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        Tutor tutor = tutorRepository.findById(request.getTutorId())
                .orElseThrow(() -> new RuntimeException("Tutor not found"));
        
        List<Subject> subjects = subjectRepository.findAllById(request.getSubjectIds());

        Set<LearningMode> modes = request.getLearningModes().stream()
                .map(LearningMode::valueOf)
                .collect(Collectors.toSet());

        OpenClass openClass = OpenClass.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .tutor(tutor)
                .subjects(subjects)
                .priceOptions(request.getPriceOptions())
                .learningModes(modes)
                .status(ClassStatus.OPEN)
                .city(request.getCity())
                .district(request.getDistrict())
                .address(request.getAddress())
                .build();

        if (request.getTimeSlots() != null) {
            List<ClassSchedule> schedules = request.getTimeSlots().stream()
                    .map(slot -> ClassSchedule.builder()
                            .startTime(slot.getStartTime())
                            .endTime(slot.getEndTime())
                            .openClass(openClass)
                            .isBooked(false)
                            .build())
                    .collect(Collectors.toList());
            openClass.setSchedules(schedules);
        }

        return mapToResponse(openClassRepository.save(openClass));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OpenClassResponse> findAllActiveClasses(String city) {
        return openClassRepository.findAll().stream()
                .filter(c -> c.getStatus() == ClassStatus.OPEN)
                // ✅ Ensure only classes from PUBLIC tutors are shown
                .filter(c -> c.getTutor().isPublic()) 
                .filter(c -> city == null || c.getCity().equalsIgnoreCase(city))
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

    /**
     * ✅ FIXED: Only returns tutors who have published their profile.
     */
    @Override
    @Transactional(readOnly = true)
    public List<TutorCardResponse> getAllPublicCards() {
        return tutorRepository.findAll().stream()
                .filter(Tutor::isPublic) // 👈 THIS LINE FILTERS OUT UNPUBLISHED TUTORS
                .map(tutor -> {
                    List<String> highlights = tutor.getEducation().stream()
                            .map(Education::getDegree)
                            .limit(2)
                            .collect(Collectors.toList());

                    return TutorCardResponse.builder()
                            .tutorId(tutor.getId())
                            .fullname(tutor.getUser() != null ? tutor.getUser().getFullname() : "Anonymous")
                            .profilePicture(tutor.getProfilePicture() != null ? tutor.getProfilePicture() : "/uploads/default.png")
                            .rating(tutor.getAverageRating() != null ? tutor.getAverageRating() : 0.0)
                            .studentsTaught(tutor.getTotalStudentsTaught() != null ? tutor.getTotalStudentsTaught() : 0)
                            .highlights(highlights)
                            .startingPrice(10.0)
                            .isPublic(tutor.isPublic())
                            .build();
                }).collect(Collectors.toList());
    }

    private OpenClassResponse mapToResponse(OpenClass entity) {
        Tutor tutor = entity.getTutor();
        
        return OpenClassResponse.builder()
                .classId(entity.getId())
                .title(entity.getTitle())
                .tutorName(tutor.getUser() != null ? tutor.getUser().getFullname() : "Unknown")
                .tutorImage(tutor.getProfilePicture() != null ? tutor.getProfilePicture() : "/uploads/default.png")
                .tutorRating(tutor.getAverageRating() != null ? tutor.getAverageRating() : 0.0)
                .pricing(entity.getPriceOptions())
                .modes(entity.getLearningModes() != null ? 
                       entity.getLearningModes().stream().map(Enum::name).collect(Collectors.toSet()) : 
                       Set.of())
                .status(entity.getStatus() != null ? entity.getStatus().name() : "CLOSED")
                .location((entity.getDistrict() != null ? entity.getDistrict() : "") + ", " + 
                          (entity.getCity() != null ? entity.getCity() : ""))
                .availableSlots(entity.getSchedules() != null ? 
                        entity.getSchedules().stream()
                            .filter(s -> !s.isBooked())
                            .map(s -> OpenClassResponse.ScheduleDto.builder()
                                    .id(s.getId())
                                    .timeRange(s.getStartTime() + " to " + s.getEndTime())
                                    .build())
                            .collect(Collectors.toList()) : List.of())
                .build();
    }
}