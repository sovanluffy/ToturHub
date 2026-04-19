package com.rental_api.ServiceBooking.Services.impl;

import com.rental_api.ServiceBooking.Dto.Request.OpenClassRequest;
import com.rental_api.ServiceBooking.Dto.Response.OpenClassResponse;
import com.rental_api.ServiceBooking.Entity.*;
import com.rental_api.ServiceBooking.Repository.*;
import com.rental_api.ServiceBooking.Services.CloudinaryService;
import com.rental_api.ServiceBooking.Services.OpenClassService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OpenClassServiceImpl implements OpenClassService {

    private final OpenClassRepository openClassRepository;
    private final TutorRepository tutorRepository;
    private final SubjectRepository subjectRepository;
    private final LocationRepository locationRepository;
    private final CloudinaryService cloudinaryService;

    // ================= CREATE =================
    @Override
    @Transactional
    public OpenClassResponse createClass(OpenClassRequest request) {
        return createClassWithImage(request, null);
    }

    @Override
    @Transactional
    public OpenClassResponse createClassWithImage(OpenClassRequest request, MultipartFile imageFile) {

        Tutor tutor = getCurrentTutor();

        OpenClass entity = new OpenClass();

        if (imageFile != null && !imageFile.isEmpty()) {
            entity.setClassImage(cloudinaryService.uploadFile(imageFile));
        }

        return save(entity, request, tutor);
    }

    // ================= UPDATE =================
    @Override
    @Transactional
    public OpenClassResponse updateClass(Long id, OpenClassRequest request) {

        OpenClass entity = openClassRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Class not found"));

        Tutor tutor = getCurrentTutor();

        if (!entity.getTutor().getId().equals(tutor.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        return save(entity, request, tutor);
    }

    // ================= GET BY ID =================
    @Override
    public OpenClassResponse getClassDetails(Long id) {
        return openClassRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new RuntimeException("Class not found"));
    }

    // ================= GET BY TUTOR =================
    @Override
    public List<OpenClassResponse> findByTutorId(Long tutorId) {
        return openClassRepository.findByTutorId(tutorId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // ================= DELETE =================
    @Override
    public void deleteClass(Long id) {
        openClassRepository.deleteById(id);
    }

    // ================= PUBLIC CARDS =================
    @Override
    public List<OpenClassResponse> getAllPublicCards() {
        return openClassRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // ================= CORE SAVE =================
    private OpenClassResponse save(OpenClass entity, OpenClassRequest request, Tutor tutor) {

        entity.setTitle(request.getTitle());
        entity.setDescription(request.getDescription());
        entity.setTutor(tutor);

        entity.setSubjects(subjectRepository.findAllById(request.getSubjectIds()));

        entity.setLocation(locationRepository.findById(request.getLocationId())
                .orElseThrow(() -> new RuntimeException("Location not found")));

        entity.setSpecificAddress(request.getSpecificAddress());
        entity.setBasePrice(request.getBasePrice());
        entity.setMaxStudents(request.getMaxStudents());

        if (request.getStatus() != null) {
            entity.setStatus(OpenClass.ClassStatus.valueOf(request.getStatus().toUpperCase()));
        }

        if (request.getClassType() != null) {
            entity.setClassType(OpenClass.ClassType.valueOf(request.getClassType().toUpperCase()));
        }

        if (request.getLearningModes() != null) {
            entity.setLearningModes(
                    request.getLearningModes().stream()
                            .map(m -> OpenClass.LearningMode.valueOf(m.toUpperCase()))
                            .collect(Collectors.toSet()));
        }

        entity.getDayTimeSlots().clear();

        if (request.getDayTimeSlots() != null) {
            request.getDayTimeSlots().forEach(slot -> entity.getDayTimeSlots().add(
                    DayTimeSlot.builder()
                            .day(slot.getDay())
                            .startTime(slot.getStartTime())
                            .endTime(slot.getEndTime())
                            .build()));
        }

        OpenClass saved = openClassRepository.save(entity);
        return mapToResponse(saved);
    }

    // ================= MAPPER =================
    private OpenClassResponse mapToResponse(OpenClass e) {

        return OpenClassResponse.builder()
                .classId(e.getId())
                .title(e.getTitle())
                .description(e.getDescription())
                .status(e.getStatus() != null ? e.getStatus().name() : null)
                .classType(e.getClassType() != null ? e.getClassType().name() : null)
                .classImage(e.getClassImage())
                .tutorId(e.getTutor().getId())
                .tutorName(e.getTutor().getUser().getFullname())
                .tutorRating(e.getTutor().getAverageRating())
                .location(e.getLocation().getDistrict() + ", " + e.getLocation().getCity())
                .specificAddress(e.getSpecificAddress())
                .subjects(e.getSubjects().stream().map(Subject::getName).toList())
                .learningModes(e.getLearningModes().stream().map(Enum::name).collect(Collectors.toSet()))
                .basePrice(e.getBasePrice())
                .maxStudents(e.getMaxStudents())
                .currentStudents(0)
                .schedules(
                        e.getDayTimeSlots().stream()
                                .map(s -> OpenClassResponse.DayTimeSlotResponse.builder()
                                        .day(s.getDay())
                                        .startTime(s.getStartTime())
                                        .endTime(s.getEndTime())
                                        .build())
                                .toList())
                .build();
    }

    // ================= CURRENT TUTOR =================
    private Tutor getCurrentTutor() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return tutorRepository.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("Tutor not found"));
    }
}