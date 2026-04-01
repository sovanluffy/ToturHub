package com.rental_api.ServiceBooking.Services.impl;

import com.rental_api.ServiceBooking.Dto.Request.OpenClassRequest;
import com.rental_api.ServiceBooking.Dto.Response.OpenClassResponse;
import com.rental_api.ServiceBooking.Entity.*;
import com.rental_api.ServiceBooking.Repository.*;
import com.rental_api.ServiceBooking.Services.OpenClassService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OpenClassServiceImpl implements OpenClassService {

    private final OpenClassRepository openClassRepository;
    private final TutorRepository tutorRepository;
    private final SubjectRepository subjectRepository;
    private final LocationRepository locationRepository;

    // ------------------- CREATE -------------------

    @Override
    @Transactional
    public OpenClassResponse createClass(OpenClassRequest request) {
        Tutor tutor = getCurrentTutor();
        return saveOrUpdateClass(new OpenClass(), request, tutor);
    }

    @Override
    @Transactional
    public OpenClassResponse createClassWithImage(OpenClassRequest request, MultipartFile imageFile) {
        // Optional: Handle file upload logic here (e.g., Cloudinary)
        OpenClassResponse response = createClass(request);
        // You can store image URL in entity if needed
        return response;
    }

    // ------------------- UPDATE -------------------

    @Override
    @Transactional
    public OpenClassResponse updateClass(Long id, OpenClassRequest request) {
        OpenClass existing = openClassRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Class not found"));

        Tutor tutor = getCurrentTutor();
        if (!existing.getTutor().getId().equals(tutor.getId())) {
            throw new RuntimeException("Unauthorized: You do not own this class");
        }

        return saveOrUpdateClass(existing, request, tutor);
    }

    private OpenClassResponse saveOrUpdateClass(OpenClass entity, OpenClassRequest request, Tutor tutor) {
        Location location = locationRepository.findById(request.getLocationId())
                .orElseThrow(() -> new RuntimeException("Location not found with ID: " + request.getLocationId()));

        entity.setTitle(request.getTitle());
        entity.setDescription(request.getDescription());
        entity.setTutor(tutor);
        entity.setLocation(location);
        entity.setSpecificAddress(request.getSpecificAddress());
        entity.setPriceOptions(request.getPriceOptions());
        entity.setSubjects(subjectRepository.findAllById(request.getSubjectIds()));
        entity.setStatus(OpenClass.ClassStatus.OPEN);

        // Learning Modes
        if (request.getLearningModes() != null) {
            Set<OpenClass.LearningMode> modes = request.getLearningModes().stream()
                    .map(mode -> OpenClass.LearningMode.valueOf(mode.toUpperCase()))
                    .collect(Collectors.toSet());
            entity.setLearningModes(modes);
        }

        // Schedules
        if (entity.getSchedules() != null) {
            entity.getSchedules().clear();
        } else {
            entity.setSchedules(new ArrayList<>());
        }

        if (request.getSchedules() != null) {
            for (OpenClassRequest.ScheduleConfig config : request.getSchedules()) {
                generateSlotsFromConfig(entity, config);
            }
        }

        return mapToResponse(openClassRepository.save(entity));
    }

    private void generateSlotsFromConfig(OpenClass entity, OpenClassRequest.ScheduleConfig config) {
        LocalDate current = config.getStartDate();
        while (!current.isAfter(config.getEndDate())) {
            DayOfWeek day = current.getDayOfWeek();
            boolean isWeekend = (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY);
            boolean shouldAdd = false;

            switch (config.getScheduleType().toUpperCase()) {
                case "DAILY":   shouldAdd = true; break;
                case "WEEKEND": shouldAdd = isWeekend; break;
                case "WEEKDAY": shouldAdd = !isWeekend; break;
            }

            if (shouldAdd && config.getTimeRanges() != null) {
                for (OpenClassRequest.TimeRangeRequest range : config.getTimeRanges()) {
                    entity.getSchedules().add(ClassSchedule.builder()
                            .startTime(current.atTime(LocalTime.parse(range.getStartTime())))
                            .endTime(current.atTime(LocalTime.parse(range.getEndTime())))
                            .openClass(entity)
                            .isBooked(false)
                            .build());
                }
            }
            current = current.plusDays(1);
        }
    }

    private OpenClassResponse mapToResponse(OpenClass entity) {
        Tutor t = entity.getTutor();
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("EEE, MMM dd");

        String tutorImage = (t.getMedia() != null) ? t.getMedia().getProfileImageUrl() : null;

        return OpenClassResponse.builder()
                .classId(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .status(entity.getStatus().name())
                .tutorId(t.getId())
                .tutorName(t.getUser().getFullname())
                .tutorImage(tutorImage)
                .tutorRating(t.getAverageRating())
                .yearsOfExperience(t.getYearsOfExperience())
                .location(entity.getLocation().getDistrict() + ", " + entity.getLocation().getCity())
                .specificAddress(entity.getSpecificAddress())
                .subjects(entity.getSubjects().stream().map(Subject::getName).collect(Collectors.toList()))
                .availableSlots(entity.getSchedules().stream()
                        .filter(s -> !s.isBooked())
                        .sorted(Comparator.comparing(ClassSchedule::getStartTime))
                        .map(s -> OpenClassResponse.ScheduleDto.builder()
                                .id(s.getId())
                                .timeRange(s.getStartTime().format(timeFmt) + " - " +
                                           s.getEndTime().format(timeFmt) + " (" +
                                           s.getStartTime().format(dateFmt) + ")")
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    // ------------------- SEARCH / FIND -------------------

    @Override
    @Transactional(readOnly = true)
    public OpenClassResponse getClassDetails(Long id) {
        return openClassRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new RuntimeException("Class not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OpenClassResponse> findByTutorId(Long tutorId) {
        return openClassRepository.findByTutorId(tutorId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ------------------- DELETE -------------------

    @Override
    @Transactional
    public void deleteClass(Long id) {
        OpenClass entity = openClassRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Class not found"));

        if (!entity.getTutor().getId().equals(getCurrentTutor().getId()))
            throw new RuntimeException("Unauthorized");

        openClassRepository.delete(entity);
    }

    // ------------------- HELPERS -------------------

    private Tutor getCurrentTutor() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return tutorRepository.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("Tutor record not found for: " + email));
    }
}