package com.rental_api.ServiceBooking.Services.impl;

import com.rental_api.ServiceBooking.Dto.Request.OpenClassRequest;
import com.rental_api.ServiceBooking.Dto.Response.OpenClassResponse;
import com.rental_api.ServiceBooking.Dto.Response.TutorCardResponse;
import com.rental_api.ServiceBooking.Entity.*;
import com.rental_api.ServiceBooking.Repository.*;
import com.rental_api.ServiceBooking.Services.OpenClassService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.nio.file.*;
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

    private static final String UPLOAD_DIR = "uploads/tutor-profiles/";

    @Override
    @Transactional
    public OpenClassResponse createClassWithImage(OpenClassRequest request, MultipartFile imageFile) {
        Tutor tutor = getCurrentTutor();
        if (imageFile != null && !imageFile.isEmpty()) {
            tutor.setProfilePicture(saveImage(imageFile));
            tutorRepository.save(tutor);
        }
        return saveOrUpdateClass(new OpenClass(), request, tutor);
    }

private OpenClassResponse saveOrUpdateClass(OpenClass entity, OpenClassRequest request, Tutor tutor) {
        Location location = locationRepository.findById(request.getLocationId())
                .orElseThrow(() -> new RuntimeException("Location not found ID: " + request.getLocationId()));

        entity.setTitle(request.getTitle());
        entity.setDescription(request.getDescription());
        entity.setTutor(tutor);
        entity.setLocation(location);
        entity.setSpecificAddress(request.getSpecificAddress());
        entity.setPriceOptions(request.getPriceOptions());
        entity.setSubjects(subjectRepository.findAllById(request.getSubjectIds()));
        entity.setStatus(OpenClass.ClassStatus.OPEN);

        if (request.getLearningModes() != null) {
            Set<OpenClass.LearningMode> modes = request.getLearningModes().stream()
                    .map(mode -> OpenClass.LearningMode.valueOf(mode.toUpperCase()))
                    .collect(Collectors.toSet());
            entity.setLearningModes(modes);
        }

        // --- FIXED: CLEARING AND REFRESHING SCHEDULES ---
        // We clear the list so orphanRemoval = true can delete old slots in the DB
        if (entity.getSchedules() != null) {
            entity.getSchedules().clear(); 
        } else {
            entity.setSchedules(new ArrayList<>());
        }

        if (request.getSchedules() != null) {
            for (OpenClassRequest.ScheduleConfig dto : request.getSchedules()) {
                generateIndividualSlots(entity, dto);
            }
        }

        // Save the entity - because of CascadeType.ALL, this saves Configs and Slots automatically
        return mapToResponse(openClassRepository.save(entity));
    }

 private void generateIndividualSlots(OpenClass openClass, OpenClassRequest.ScheduleConfig dto) {
    // 1. Create and populate the Config object
    ScheduleConfig config = new ScheduleConfig();
    
    // Set the string directly from the DTO
    String typeStr = dto.getScheduleType() != null ? dto.getScheduleType().toUpperCase() : "DAILY";
    config.setScheduleType(typeStr); 
    
    config.setStartDate(dto.getStartDate());
    config.setEndDate(dto.getEndDate());
    config.setOpenClass(openClass);

    // 2. Set the 'Main' times for the config table (to avoid [null] in image 2)
    if (dto.getTimeRanges() != null && !dto.getTimeRanges().isEmpty()) {
        config.setStartTime(parseTimeSafely(dto.getTimeRanges().get(0).getStartTime()));
        config.setEndTime(parseTimeSafely(dto.getTimeRanges().get(0).getEndTime()));
    }

    // 3. Generate the Slots
    LocalDate current = dto.getStartDate();
    while (!current.isAfter(dto.getEndDate())) {
        DayOfWeek day = current.getDayOfWeek();
        boolean isWeekend = (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY);
        
        // Use the local variable 'typeStr' to avoid the getter error during logic
        boolean shouldAdd = switch (typeStr) {
            case "DAILY"   -> true;
            case "WEEKEND" -> isWeekend;
            case "WEEKDAY" -> !isWeekend;
            case "ONCE"    -> current.equals(dto.getStartDate());
            default        -> false;
        };

        if (shouldAdd) {
            for (OpenClassRequest.TimeRangeRequest trDto : dto.getTimeRanges()) {
                LocalTime startT = parseTimeSafely(trDto.getStartTime());
                LocalTime endT = parseTimeSafely(trDto.getEndTime());

                ClassSchedule slot = ClassSchedule.builder()
                        .startTime(current.atTime(startT))
                        .endTime(current.atTime(endT))
                        .openClass(openClass)
                        .config(config) // THIS LINE fills the schedule_config_id column
                        .isBooked(false)
                        .type(mapToScheduleType(typeStr))
                        .build();
                
                config.getIndividualSlots().add(slot);
            }
        }
        current = current.plusDays(1);
    }
    
    openClass.getSchedules().add(config);
}

    // Helper to prevent DateTimeParseException if user sends "8:00" instead of "08:00"
    private LocalTime parseTimeSafely(String timeStr) {
        if (timeStr.length() == 4) timeStr = "0" + timeStr;
        return LocalTime.parse(timeStr);
    }

    private ClassSchedule.ScheduleType mapToScheduleType(String type) {
        try {
            return ClassSchedule.ScheduleType.valueOf(type.toUpperCase());
        } catch (Exception e) {
            return ClassSchedule.ScheduleType.DAILY;
        }
    }

    private OpenClassResponse mapToResponse(OpenClass entity) {
        Tutor t = entity.getTutor();
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("EEE, MMM dd");

        List<OpenClassResponse.ScheduleDto> availableSlots = entity.getSchedules().stream()
                .flatMap(config -> config.getIndividualSlots().stream())
                .filter(s -> !s.isBooked())
                .sorted(Comparator.comparing(ClassSchedule::getStartTime))
                .map(s -> OpenClassResponse.ScheduleDto.builder()
                        .id(s.getId())
                        .timeRange(s.getStartTime().format(timeFmt) + " - " +
                                   s.getEndTime().format(timeFmt) + " (" +
                                   s.getStartTime().format(dateFmt) + ")")
                        .build())
                .collect(Collectors.toList());

        return OpenClassResponse.builder()
                .classId(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .status(entity.getStatus().name())
                .tutorId(t.getId())
                .tutorName(t.getUser().getFullname())
                .tutorImage(t.getProfilePicture())
                .tutorRating(t.getAverageRating())
                .yearsOfExperience(t.getYearsOfExperience())
                .location(entity.getLocation().getDistrict() + ", " + entity.getLocation().getCity())
                .specificAddress(entity.getSpecificAddress())
                .subjects(entity.getSubjects().stream().map(Subject::getName).collect(Collectors.toList()))
                .pricing(entity.getPriceOptions())
                .availableSlots(availableSlots)
                .build();
    }

    // --- Standard Repository Methods ---

    @Override
    public OpenClassResponse createClass(OpenClassRequest request) {
        return createClassWithImage(request, null);
    }

    @Override
    @Transactional(readOnly = true)
    public OpenClassResponse getClassDetails(Long id) {
        return openClassRepository.findById(id).map(this::mapToResponse)
                .orElseThrow(() -> new RuntimeException("Class not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OpenClassResponse> findByTutorId(Long tutorId) {
        return openClassRepository.findByTutorId(tutorId).stream()
                .map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public List<TutorCardResponse> getAllPublicCards() {
        return tutorRepository.findAll().stream()
                .filter(Tutor::isPublic)
                .map(this::mapToTutorCard).collect(Collectors.toList());
    }

    private TutorCardResponse mapToTutorCard(Tutor tutor) {
        String loc = tutor.getOpenClasses().isEmpty() ? "Not Specified" :
                tutor.getOpenClasses().get(0).getLocation().getDistrict();
        
        return TutorCardResponse.builder()
                .tutorId(tutor.getId())
                .fullname(tutor.getUser().getFullname())
                .profilePicture(tutor.getProfilePicture())
                .rating(tutor.getAverageRating())
                .subjects(tutor.getOpenClasses().stream().flatMap(c -> c.getSubjects().stream()).map(Subject::getName).distinct().toList())
                .location(loc)
                .build();
    }

    @Override
    @Transactional
    public void deleteClass(Long id) {
        OpenClass entity = openClassRepository.findById(id).orElseThrow();
        if (!entity.getTutor().getId().equals(getCurrentTutor().getId())) throw new RuntimeException("Unauthorized");
        openClassRepository.delete(entity);
    }

    @Override
    @Transactional
    public OpenClassResponse updateClass(Long id, OpenClassRequest request) {
        OpenClass existing = openClassRepository.findById(id).orElseThrow();
        if (!existing.getTutor().getId().equals(getCurrentTutor().getId())) throw new RuntimeException("Unauthorized");
        return saveOrUpdateClass(existing, request, existing.getTutor());
    }

    private Tutor getCurrentTutor() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return tutorRepository.findByUserEmail(email).orElseThrow();
    }

    private String saveImage(MultipartFile file) {
        try {
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path path = Paths.get(UPLOAD_DIR + fileName);
            Files.createDirectories(path.getParent());
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            return "/" + UPLOAD_DIR + fileName;
        } catch (IOException e) { throw new RuntimeException(e); }
    }
}