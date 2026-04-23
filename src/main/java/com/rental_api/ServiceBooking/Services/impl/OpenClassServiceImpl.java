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
import com.rental_api.ServiceBooking.Specification.OpenClassSpecification;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OpenClassServiceImpl implements OpenClassService {

    private final OpenClassRepository openClassRepository;
    private final TutorRepository tutorRepository;
    private final SubjectRepository subjectRepository;
    private final LocationRepository locationRepository;
    private final CloudinaryService cloudinaryService;
    private final DayTimeSlotRepository dayTimeSlotRepository;

    // =========================================================
    // CREATE
    // =========================================================

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

        OpenClass saved = save(entity, request, tutor);
        return mapToResponse(saved);
    }

    // =========================================================
    // SAVE CORE
    // =========================================================

    private OpenClass save(OpenClass entity, OpenClassRequest request, Tutor tutor) {

        entity.setTitle(request.getTitle());
        entity.setDescription(request.getDescription());
        entity.setTutor(tutor);

        entity.setSubjects(subjectRepository.findAllById(request.getSubjectIds()));

        entity.setLocation(locationRepository.findById(request.getLocationId())
                .orElseThrow(() -> new RuntimeException("Location not found")));

        entity.setSpecificAddress(request.getSpecificAddress());
        entity.setBasePrice(request.getBasePrice());
        entity.setMaxStudents(request.getMaxStudents());

        // learning modes
        if (request.getLearningModes() != null) {
            entity.setLearningModes(
                    request.getLearningModes()
                            .stream()
                            .map(m -> OpenClass.LearningMode.valueOf(m.toUpperCase()))
                            .collect(Collectors.toSet())
            );
        }

        entity.setStatus(
                request.getStatus() != null
                        ? OpenClass.ClassStatus.valueOf(request.getStatus().toUpperCase())
                        : OpenClass.ClassStatus.OPEN
        );

        entity.setVisibilityStatus(OpenClass.VisibilityStatus.PUBLIC);

        OpenClass saved = openClassRepository.save(entity);

        // schedules
        if (request.getDayTimeSlots() != null && !request.getDayTimeSlots().isEmpty()) {

            List<DayTimeSlot> slots = request.getDayTimeSlots().stream()
                    .map(s -> DayTimeSlot.builder()
                            .day(s.getDay())
                            .startTime(s.getStartTime())
                            .endTime(s.getEndTime())
                            .maxStudents(s.getMaxStudents() != null ? s.getMaxStudents() : 10)
                            .bookedCount(0)
                            .openClass(saved)
                            .build())
                    .toList();

            dayTimeSlotRepository.saveAll(slots);
            saved.setDayTimeSlots(slots);
        }

        return saved;
    }

    // =========================================================
    // UPDATE
    // =========================================================

    @Override
    @Transactional
    public OpenClassResponse updateClass(Long id, OpenClassRequest request) {

        OpenClass entity = openClassRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Class not found"));

        checkOwner(entity);

        OpenClass updated = save(entity, request, entity.getTutor());
        return mapToResponse(updated);
    }

    // =========================================================
    // DELETE
    // =========================================================

    @Override
    public void deleteClass(Long id) {

        OpenClass entity = openClassRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Class not found"));

        checkOwner(entity);

        openClassRepository.delete(entity);
    }

    // =========================================================
    // GET BY ID
    // =========================================================

    @Override
    public OpenClassResponse getClassDetails(Long id) {
        return openClassRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new RuntimeException("Class not found"));
    }

    // =========================================================
    // PUBLIC FEED
    // =========================================================

    @Override
    public List<OpenClassResponse> getAllPublicCards() {
        return openClassRepository.findAllPublicFeed(
                        OpenClass.ClassStatus.OPEN,
                        OpenClass.VisibilityStatus.PUBLIC
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // =========================================================
    // TUTOR CLASSES
    // =========================================================

    @Override
    public List<OpenClassResponse> findByTutorId(Long tutorId) {
        return openClassRepository.findByTutorId(tutorId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<OpenClassResponse> getPublicClassesByTutor(Long tutorId) {
        return openClassRepository.findPublicClassesByTutorId(
                        tutorId,
                        OpenClass.ClassStatus.OPEN,
                        OpenClass.VisibilityStatus.PUBLIC
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // =========================================================
    // END CLASS
    // =========================================================

    @Override
    @Transactional
    public OpenClassResponse endClass(Long id) {

        OpenClass entity = openClassRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Class not found"));

        checkOwner(entity);

        entity.setStatus(OpenClass.ClassStatus.CLOSED);

        return mapToResponse(openClassRepository.save(entity));
    }

    // =========================================================
    // REOPEN CLASS
    // =========================================================

    @Override
    @Transactional
    public OpenClassResponse reopenClass(Long id) {

        OpenClass entity = openClassRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Class not found"));

        checkOwner(entity);

        entity.setStatus(OpenClass.ClassStatus.OPEN);

        return mapToResponse(openClassRepository.save(entity));
    }

    // =========================================================
    // COPY CLASS (SAFE DEEP COPY)
    // =========================================================

    @Override
    @Transactional
    public OpenClassResponse copyClass(Long id) {

        OpenClass original = openClassRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Class not found"));

        checkOwner(original);

        OpenClass copy = new OpenClass();

        copy.setTitle(original.getTitle() + " (Copy)");
        copy.setDescription(original.getDescription());
        copy.setTutor(original.getTutor());
        copy.setLocation(original.getLocation());
        copy.setSpecificAddress(original.getSpecificAddress());
        copy.setBasePrice(original.getBasePrice());
        copy.setMaxStudents(original.getMaxStudents());
        copy.setClassImage(original.getClassImage());

        copy.setSubjects(original.getSubjects() != null
                ? new ArrayList<>(original.getSubjects())
                : new ArrayList<>());

        copy.setLearningModes(original.getLearningModes() != null
                ? new HashSet<>(original.getLearningModes())
                : new HashSet<>());

        copy.setStatus(OpenClass.ClassStatus.OPEN);
        copy.setVisibilityStatus(OpenClass.VisibilityStatus.PUBLIC);

        OpenClass saved = openClassRepository.save(copy);

        if (original.getDayTimeSlots() != null) {

            List<DayTimeSlot> slots = original.getDayTimeSlots().stream()
                    .map(s -> DayTimeSlot.builder()
                            .day(s.getDay())
                            .startTime(s.getStartTime())
                            .endTime(s.getEndTime())
                            .maxStudents(s.getMaxStudents())
                            .bookedCount(0)
                            .openClass(saved)
                            .build())
                    .toList();

            dayTimeSlotRepository.saveAll(slots);
            saved.setDayTimeSlots(slots);
        }

        return mapToResponse(saved);
    }

    // =========================================================
    // MAPPER (FIXED NULL SAFE + isNew FIX)
    // =========================================================

    private OpenClassResponse mapToResponse(OpenClass e) {

        long confirmedCount = openClassRepository.countConfirmedStudentsByClassId(e.getId());

        return OpenClassResponse.builder()
                .classId(e.getId())
                .title(e.getTitle())
                .description(e.getDescription())
                .status(e.getStatus() != null ? e.getStatus().name() : null)
                .visibilityStatus(e.getVisibilityStatus() != null ? e.getVisibilityStatus().name() : null)

                .tutor(OpenClassResponse.TutorPublicResponse.builder()
                        .tutorId(e.getTutor().getId())
                        .name(e.getTutor().getUser().getFullname())
                        .avatar(e.getTutor().getUser().getAvatarUrl())
                        .rating(e.getTutor().getAverageRating())
                        .email(e.getTutor().getUser().getEmail())
                        .phone(e.getTutor().getUser().getPhone())
                        .build())

                .location(e.getLocation().getDistrict() + ", " + e.getLocation().getCity())
                .specificAddress(e.getSpecificAddress())

                .subjects(e.getSubjects() != null
                        ? e.getSubjects().stream().map(Subject::getName).toList()
                        : List.of())

                .learningModes(e.getLearningModes() != null
                        ? e.getLearningModes().stream().map(Enum::name).collect(Collectors.toSet())
                        : Set.of())

                .basePrice(e.getBasePrice())
                .maxStudents(e.getMaxStudents())
                .currentStudents((int) confirmedCount)
                .classImage(e.getClassImage())

                .schedules(e.getDayTimeSlots() != null
                        ? e.getDayTimeSlots().stream().map(this::mapSlot).toList()
                        : List.of())

                // ✅ FIXED ERROR HERE
                .isNew(e.isNew())

                .build();
    }

    private OpenClassResponse.DayTimeSlotResponse mapSlot(DayTimeSlot s) {
        return OpenClassResponse.DayTimeSlotResponse.builder()
                .id(s.getId())
                .day(s.getDay())
                .startTime(s.getStartTime())
                .endTime(s.getEndTime())
                .maxStudents(s.getMaxStudents())
                .bookedCount(s.getBookedCount())
                .build();
    }

    // =========================================================
    // SECURITY
    // =========================================================

    private void checkOwner(OpenClass entity) {
        Tutor tutor = getCurrentTutor();
        if (!entity.getTutor().getId().equals(tutor.getId())) {
            throw new RuntimeException("Unauthorized");
        }
    }

    private Tutor getCurrentTutor() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return tutorRepository.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("Tutor not found"));
    }
//code new
    @Override
    @Transactional(readOnly = true)
    public List<OpenClassResponse> filterOpenClasses(String location, String subject) {
    return openClassRepository.findAll(
            OpenClassSpecification.filter(location, subject)
    ).stream()
     .map(this::mapToResponse)
     .toList();
    }

}