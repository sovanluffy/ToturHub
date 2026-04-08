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

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OpenClassServiceImpl implements OpenClassService {

    private final OpenClassRepository openClassRepository;
    private final TutorRepository tutorRepository;
    private final SubjectRepository subjectRepository;
    private final LocationRepository locationRepository;

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
        // Image upload logic (S3/Cloudinary) would be implemented here
        return saveOrUpdateClass(entity, request, tutor);
    }

    @Override
    @Transactional
    public OpenClassResponse updateClass(Long id, OpenClassRequest request) {
        OpenClass existing = openClassRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Class not found with ID: " + id));

        Tutor tutor = getCurrentTutor();
        if (!existing.getTutor().getId().equals(tutor.getId())) {
            throw new RuntimeException("Unauthorized: You do not own this class");
        }
        return saveOrUpdateClass(existing, request, tutor);
    }

    private OpenClassResponse saveOrUpdateClass(OpenClass entity, OpenClassRequest request, Tutor tutor) {
        Location location = locationRepository.findById(request.getLocationId())
                .orElseThrow(() -> new RuntimeException("Location not found"));

        entity.setTitle(request.getTitle());
        entity.setDescription(request.getDescription());
        entity.setTutor(tutor);
        entity.setLocation(location);
        entity.setSpecificAddress(request.getSpecificAddress());
        entity.setBasePrice(request.getBasePrice());
        entity.setMaxStudents(request.getMaxStudents() != null ? request.getMaxStudents() : 20);
        entity.setSubjects(subjectRepository.findAllById(request.getSubjectIds()));
        entity.setStatus(OpenClass.ClassStatus.OPEN);

        // Map Learning Modes
        if (request.getLearningModes() != null) {
            Set<OpenClass.LearningMode> modes = request.getLearningModes().stream()
                    .map(mode -> OpenClass.LearningMode.valueOf(mode.toUpperCase().trim()))
                    .collect(Collectors.toSet());
            entity.setLearningModes(modes);
        }

        // Handle Schedule Logic
        if (entity.getSchedules() == null) {
            entity.setSchedules(new ArrayList<>());
        } else {
            entity.getSchedules().clear();
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
        String typeStr = (dto.getScheduleType() != null) ? dto.getScheduleType().toUpperCase() : "DAILY";
        
        config.setScheduleType(typeStr); 
        config.setStartDate(dto.getStartDate());
        config.setEndDate(dto.getEndDate());
        config.setOpenClass(openClass);
        config.setIndividualSlots(new ArrayList<>());

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

            if (shouldAdd && dto.getTimeRanges() != null) {
                for (OpenClassRequest.TimeRangeRequest trDto : dto.getTimeRanges()) {
                    ClassSchedule slot = ClassSchedule.builder()
                            .startTime(current.atTime(parseTimeSafely(trDto.getStartTime())))
                            .endTime(current.atTime(parseTimeSafely(trDto.getEndTime())))
                            .openClass(openClass)
                            .config(config)
                            .booked(false) 
                            .type(mapToScheduleType(typeStr))
                            .build();
                    config.getIndividualSlots().add(slot);
                }
            }
            current = current.plusDays(1);
        }
        openClass.getSchedules().add(config);
    }

    private OpenClassResponse mapToResponse(OpenClass entity) {
        Tutor t = entity.getTutor();

        // 1. Calculate Capacity
        int totalConfirmedStudents = (int) entity.getSchedules().stream()
            .flatMap(config -> config.getBookings().stream())
            .filter(b -> "CONFIRMED".equalsIgnoreCase(b.getStatus().toString()))
            .count();

        // 2. Generate Price Tiers
        List<OpenClassResponse.PriceTierDto> priceOptions = generatePriceTiers(entity.getBasePrice(), entity.getMaxStudents());

        // 3. Map Schedules for the Frontend
        List<OpenClassResponse.ScheduleConfigResponse> schedules = entity.getSchedules().stream()
            .flatMap(config -> config.getIndividualSlots().stream()
                .map(slot -> OpenClassResponse.ScheduleConfigResponse.builder()
                    .id(slot.getId()) 
                    .scheduleType(config.getScheduleType())
                    .startDate(slot.getStartTime().toLocalDate())
                    .endDate(slot.getEndTime().toLocalDate())
                    .startTime(slot.getStartTime().toLocalTime().toString())
                    .endTime(slot.getEndTime().toLocalTime().toString())
                    .build()))
            .sorted(Comparator.comparing(OpenClassResponse.ScheduleConfigResponse::getStartDate))
            .collect(Collectors.toList());

        return OpenClassResponse.builder()
                .classId(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .status(entity.getStatus().name())
                .tutorId(t.getId())
                .tutorName(t.getUser().getFullname())
                .tutorRating(t.getAverageRating())
                .location(entity.getLocation().getDistrict() + ", " + entity.getLocation().getCity())
                .specificAddress(entity.getSpecificAddress())
                .subjects(entity.getSubjects().stream().map(Subject::getName).toList())
                .basePrice(entity.getBasePrice())
                .maxStudents(entity.getMaxStudents())
                .currentStudents(totalConfirmedStudents)
                .priceOptions(priceOptions)
                .schedules(schedules) 
                .learningModes(entity.getLearningModes() != null ? 
                    entity.getLearningModes().stream().map(Enum::name).collect(Collectors.toSet()) : 
                    Collections.emptySet())
                .build();
    }

    private List<OpenClassResponse.PriceTierDto> generatePriceTiers(BigDecimal base, int max) {
        List<OpenClassResponse.PriceTierDto> tiers = new ArrayList<>();
        tiers.add(new OpenClassResponse.PriceTierDto("1 Student", base));

        for (int start = 5; start <= max; start += 5) {
            int end = Math.min(start + 4, max);
            double discountRate = Math.min((start / 5) * 0.10, 0.50); 
            BigDecimal tierPrice = base.multiply(BigDecimal.valueOf(1.0 - discountRate));
            
            String label = (start == end) ? start + " Students" : start + "-" + end + " Students";
            tiers.add(new OpenClassResponse.PriceTierDto(label, tierPrice));
            if (end == max) break;
        }
        return tiers;
    }

    private LocalTime parseTimeSafely(String timeStr) {
        try {
            if (timeStr.contains(":")) {
                return LocalTime.parse(timeStr);
            }
            // Handle "0800" or "800" formats
            if (timeStr.length() == 3) timeStr = "0" + timeStr;
            String formatted = timeStr.substring(0, 2) + ":" + timeStr.substring(2);
            return LocalTime.parse(formatted);
        } catch (Exception e) {
            return LocalTime.of(9, 0); 
        }
    }

    private ClassSchedule.ScheduleType mapToScheduleType(String type) {
        try { return ClassSchedule.ScheduleType.valueOf(type.toUpperCase()); }
        catch (Exception e) { return ClassSchedule.ScheduleType.DAILY; }
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
        String loc = tutor.getOpenClasses().isEmpty() ? "Multiple Locations" :
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

    @Override
    @Transactional
    public void deleteClass(Long id) {
        OpenClass entity = openClassRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Class not found"));
        if (!entity.getTutor().getId().equals(getCurrentTutor().getId())) {
            throw new RuntimeException("Unauthorized: You cannot delete this class");
        }
        openClassRepository.delete(entity);
    }

    private Tutor getCurrentTutor() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return tutorRepository.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("Tutor profile not found for email: " + email));
    }
}