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
        
        Tutor tutor = getCurrentTutor();
        OpenClass entity = new OpenClass();
        // Set image URL to entity if your entity has an image field
        // entity.setImageUrl(imageUrl); 
        
        return saveOrUpdateClass(entity, request, tutor);
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

        // Clear and refresh schedules to avoid duplicates
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

        return mapToResponse(openClassRepository.save(entity));
    }

    private void generateIndividualSlots(OpenClass openClass, OpenClassRequest.ScheduleConfig dto) {
        ScheduleConfig config = new ScheduleConfig();
        String typeStr = dto.getScheduleType() != null ? dto.getScheduleType().toUpperCase() : "DAILY";
        
        config.setScheduleType(typeStr); 
        config.setStartDate(dto.getStartDate());
        config.setEndDate(dto.getEndDate());
        config.setOpenClass(openClass);

        if (dto.getTimeRanges() != null && !dto.getTimeRanges().isEmpty()) {
            config.setStartTime(parseTimeSafely(dto.getTimeRanges().get(0).getStartTime()));
            config.setEndTime(parseTimeSafely(dto.getTimeRanges().get(0).getEndTime()));
        }

        LocalDate current = dto.getStartDate();
        while (!current.isAfter(dto.getEndDate())) {
            DayOfWeek day = current.getDayOfWeek();
            boolean isWeekend = (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY);
            
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
                            .config(config)
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

        // Use the flattened logic from HEAD to get specific date slots
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
                .pricing(entity.getPriceOptions())
                .availableSlots(availableSlots)
                .build();
    }

    // ------------------- READ & SEARCH -------------------

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
                .profilePicture(tutor.getMedia() != null ? tutor.getMedia().getProfileImageUrl() : null)
                .rating(tutor.getAverageRating())
                .subjects(tutor.getOpenClasses().stream()
                        .flatMap(c -> c.getSubjects().stream())
                        .map(Subject::getName).distinct().toList())
                .location(loc)
                .build();
    }

    // ------------------- DELETE -------------------

    @Override
    @Transactional
    public void deleteClass(Long id) {
        OpenClass entity = openClassRepository.findById(id).orElseThrow();
        if (!entity.getTutor().getId().equals(getCurrentTutor().getId())) {
            throw new RuntimeException("Unauthorized");
        }
        openClassRepository.delete(entity);
    }

    private Tutor getCurrentTutor() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return tutorRepository.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("Tutor profile not found for user: " + email));
    }

    private String saveImage(MultipartFile file) {
        try {
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path path = Paths.get(UPLOAD_DIR + fileName);
            Files.createDirectories(path.getParent());
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            return "/" + UPLOAD_DIR + fileName;
        } catch (IOException e) { 
            throw new RuntimeException("Failed to store image", e); 
        }
    }
}