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

        // Convert String modes from Request to Enum Set
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

        // Map Time Slots if provided
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

    // --- NEW: SEARCH LOGIC ---
    @Override
    @Transactional(readOnly = true)
    public List<OpenClassResponse> findAllActiveClasses(String city) {
        return openClassRepository.findAll().stream()
                .filter(c -> c.getStatus() == ClassStatus.OPEN)
                .filter(c -> city == null || c.getCity().equalsIgnoreCase(city))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // --- NEW: GET DETAILS LOGIC ---
    @Override
    @Transactional(readOnly = true)
    public OpenClassResponse getClassDetails(Long id) {
        OpenClass entity = openClassRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Class not found"));
        return mapToResponse(entity);
    }

    private OpenClassResponse mapToResponse(OpenClass entity) {
        Tutor tutor = entity.getTutor();
        
        return OpenClassResponse.builder()
                .classId(entity.getId())
                .title(entity.getTitle())
                // Profile data included in class view
                .tutorName(tutor.getUser().getFullname())
                .tutorImage(tutor.getProfilePicture())
                .tutorRating(tutor.getAverageRating())
                .pricing(entity.getPriceOptions())
                .modes(entity.getLearningModes().stream().map(Enum::name).collect(Collectors.toSet()))
                .status(entity.getStatus().name())
                .location(entity.getDistrict() + ", " + entity.getCity())
                // Map the clickable slots
                .availableSlots(entity.getSchedules().stream()
                        .filter(s -> !s.isBooked())
                        .map(s -> OpenClassResponse.ScheduleDto.builder()
                                .id(s.getId())
                                .timeRange(s.getStartTime() + " to " + s.getEndTime())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    @Override
@Transactional(readOnly = true)
public List<TutorCardResponse> getAllPublicCards() {
    // 1. Fetch all classes from DB
    return openClassRepository.findAll().stream()
            // 2. Only show classes that are still "OPEN"
            .filter(c -> c.getStatus() == OpenClass.ClassStatus.OPEN)
            // 3. Convert Entity to DTO
            .map(this::mapToCardResponse)
            .collect(Collectors.toList());
}

private TutorCardResponse mapToCardResponse(OpenClass entity) {
    // Safety check for null tutor/user to avoid NullPointerException
    String name = (entity.getTutor() != null && entity.getTutor().getUser() != null) 
                  ? entity.getTutor().getUser().getFullname() : "Unknown Tutor";
    
    String profilePic = (entity.getTutor() != null) ? entity.getTutor().getProfilePicture() : null;
    Double tutorRating = (entity.getTutor() != null) ? entity.getTutor().getAverageRating() : 0.0;

    return TutorCardResponse.builder()
            .classId(entity.getId())
            .title(entity.getTitle())
            .tutorName(name)
            .tutorImage(profilePic)
            .rating(tutorRating)
            .location(entity.getCity() + ", " + entity.getDistrict())
            .priceOptions(entity.getPriceOptions())
            .build();
}
}