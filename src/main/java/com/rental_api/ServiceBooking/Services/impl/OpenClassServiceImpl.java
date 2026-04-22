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
import java.util.Set;
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
    private final BookingRepository bookingRepository;

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

        // ================= IMAGE =================
        if (imageFile != null && !imageFile.isEmpty()) {
            String imageUrl = cloudinaryService.uploadFile(imageFile);
            entity.setClassImage(imageUrl);
        }

        OpenClass saved = save(entity, request, tutor);

        return mapToResponse(saved);
    }

    // ================= SAVE CORE =================
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

        if (request.getStatus() != null) {
            entity.setStatus(OpenClass.ClassStatus.valueOf(request.getStatus().toUpperCase()));
        }

        OpenClass saved = openClassRepository.save(entity);

        // ================= SCHEDULE =================
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
                    .collect(Collectors.toList());

            dayTimeSlotRepository.saveAll(slots);
            saved.setDayTimeSlots(slots);
        }

        return saved;
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

        OpenClass updated = save(entity, request, tutor);

        return mapToResponse(updated);
    }

    // ================= DELETE =================
    @Override
    public void deleteClass(Long id) {
        openClassRepository.deleteById(id);
    }

    // ================= GET BY ID =================
    @Override
    public OpenClassResponse getClassDetails(Long id) {
        return openClassRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new RuntimeException("Class not found"));
    }

    // ================= GET ALL PUBLIC =================
    @Override
    public List<OpenClassResponse> getAllPublicCards() {
        return openClassRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ================= GET BY TUTOR =================
    @Override
    public List<OpenClassResponse> findByTutorId(Long tutorId) {
        return openClassRepository.findByTutorId(tutorId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ================= MAP RESPONSE =================
    private OpenClassResponse mapToResponse(OpenClass e) {

        // ================= CONFIRMED STUDENTS =================
        List<BookingClass> bookings =
                bookingRepository.findByOpenClass_Id(e.getId());

        List<OpenClassResponse.StudentPublicResponse> confirmedStudents =
                bookings.stream()
                        .filter(b -> b.getStatus() != null
                                && b.getStatus().name().equals("CONFIRMED"))
                        .map(b -> OpenClassResponse.StudentPublicResponse.builder()
                                .studentId(b.getUser().getId())
                                .studentName(b.getUser().getFullname())
                                .avatar(b.getUser().getAvatarUrl())
                                .email(b.getUser().getEmail())
                                .build())
                        .collect(Collectors.toList());

        // ================= TUTOR FULL PROFILE =================
        OpenClassResponse.TutorPublicResponse tutor =
                OpenClassResponse.TutorPublicResponse.builder()
                        .tutorId(e.getTutor().getId())
                        .name(e.getTutor().getUser().getFullname())
                        .avatar(e.getTutor().getUser().getAvatarUrl())
                        .rating(e.getTutor().getAverageRating())
                        .email(e.getTutor().getUser().getEmail())
                        .phone(e.getTutor().getUser().getPhone())
                        .build();

        return OpenClassResponse.builder()
                .classId(e.getId())
                .title(e.getTitle())
                .description(e.getDescription())
                .status(e.getStatus() != null ? e.getStatus().name() : null)

                // tutor
                .tutor(tutor)

                // location
                .location(e.getLocation().getDistrict() + ", " + e.getLocation().getCity())
                .specificAddress(e.getSpecificAddress())

                // subjects
                .subjects(e.getSubjects() != null
                        ? e.getSubjects().stream()
                        .map(Subject::getName)
                        .collect(Collectors.toList())
                        : List.of())

                // learning modes
                .learningModes(e.getLearningModes() != null
                        ? e.getLearningModes().stream()
                        .map(Enum::name)
                        .collect(Collectors.toSet())
                        : Set.of())

                .basePrice(e.getBasePrice())
                .maxStudents(e.getMaxStudents())

                // students
                .currentStudents(confirmedStudents.size())
                .confirmedStudents(confirmedStudents)

                // image
                .classImage(e.getClassImage())

                // schedules
                .schedules(e.getDayTimeSlots() != null
                        ? e.getDayTimeSlots().stream()
                        .map(this::mapSlot)
                        .collect(Collectors.toList())
                        : List.of())

                .build();
    }

    // ================= SLOT =================
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

    // ================= CURRENT TUTOR =================
    private Tutor getCurrentTutor() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return tutorRepository.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("Tutor not found"));
    }
}