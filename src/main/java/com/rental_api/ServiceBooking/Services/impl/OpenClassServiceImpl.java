package com.rental_api.ServiceBooking.Services.impl;

import com.rental_api.ServiceBooking.Dto.Request.OpenClassRequest;
import com.rental_api.ServiceBooking.Dto.Response.OpenClassResponse;
import com.rental_api.ServiceBooking.Entity.*;
import com.rental_api.ServiceBooking.Repository.*;
import com.rental_api.ServiceBooking.Services.CloudinaryService;
import com.rental_api.ServiceBooking.Services.OpenClassService;
import com.rental_api.ServiceBooking.Specification.OpenClassSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
    private final BookingRepository bookingRepository; 

    // =========================================================
    // 🔍 GET STUDENTS BY CLASS ID
    // =========================================================
    @Override
    @Transactional(readOnly = true)
    public List<OpenClassResponse.StudentPublicResponse> getStudentsByClass(Long classId) {
        if (!openClassRepository.existsById(classId)) {
            throw new RuntimeException("Class not found");
        }

        // Uses BookingClass to match your Repository
        return bookingRepository.findConfirmedStudentsByClassId(classId)
                .stream()
                .map(this::mapToStudentResponse)
                .toList();
    }

    // =========================================================
    // 🔄 FIXED: STUDENT MAPPER (Uses BookingClass)
    // =========================================================
    private OpenClassResponse.StudentPublicResponse mapToStudentResponse(BookingClass b) {
        return OpenClassResponse.StudentPublicResponse.builder()
                .studentId(b.getUser().getId())
                .studentName(b.getUser().getFullname())
                .avatar(b.getUser().getAvatarUrl())
                .email(b.getUser().getEmail())
                // Maps the specific schedule the student booked
                .bookedSchedule(OpenClassResponse.BookedScheduleInfo.builder()
                        .day(b.getSchedule().getDay()) // Using .getSchedule() per your repo FETCH
                        .startTime(b.getSchedule().getStartTime())
                        .endTime(b.getSchedule().getEndTime())
                        .build())
                .build();
    }

    // =========================================================
    // 🔄 MAPPER (POPULATES STUDENT DATA)
    // =========================================================
    private OpenClassResponse mapToResponse(OpenClass e) {
        List<OpenClassResponse.StudentPublicResponse> studentInfo = bookingRepository
                .findConfirmedStudentsByClassId(e.getId())
                .stream()
                .map(this::mapToStudentResponse)
                .toList();

        return OpenClassResponse.builder()
                .classId(e.getId())
                .title(e.getTitle())
                .description(e.getDescription())
                .classImage(e.getClassImage())
                .status(e.getStatus() != null ? e.getStatus().name() : null)
                .visibilityStatus(e.getVisibilityStatus() != null ? e.getVisibilityStatus().name() : null)
                .startDate(e.getStartDate())
                .endDate(e.getEndDate())
                .durationType(e.getDurationType())
                .durationValue(e.getDurationValue())
                .tutor(OpenClassResponse.TutorPublicResponse.builder()
                        .tutorId(e.getTutor().getId())
                        .name(e.getTutor().getUser().getFullname())
                        .avatar(e.getTutor().getUser().getAvatarUrl())
                        .rating(e.getTutor().getAverageRating())
                        .email(e.getTutor().getUser().getEmail())
                        .phone(e.getTutor().getUser().getPhone()).build())
                .location(e.getLocation().getDistrict() + ", " + e.getLocation().getCity())
                .specificAddress(e.getSpecificAddress())
                .subjects(e.getSubjects() != null ? e.getSubjects().stream().map(Subject::getName).toList() : List.of())
                .learningModes(e.getLearningModes() != null ? e.getLearningModes().stream().map(Enum::name).collect(Collectors.toSet()) : Set.of())
                .basePrice(e.getBasePrice())
                .maxStudents(e.getMaxStudents())
                .currentStudents(studentInfo.size())
                .confirmedStudents(studentInfo) 
                .schedules(e.getDayTimeSlots() != null ? e.getDayTimeSlots().stream().map(this::mapSlot).toList() : List.of())
                .isNew(e.isNew())
                .build();
    }

    // =========================================================
    // 🟢 CREATE / SAVE / UPDATE / DELETE / COPY
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
        entity.setStartDate(request.getStartDate());
        entity.setDurationType(request.getDurationType());
        entity.setDurationValue(request.getDurationValue());

        if (request.getStartDate() != null) entity.setEndDate(entity.calculateEndDate());

        if (request.getLearningModes() != null) {
            entity.setLearningModes(request.getLearningModes().stream()
                    .map(m -> OpenClass.LearningMode.valueOf(m.toUpperCase()))
                    .collect(Collectors.toSet()));
        }

        entity.setStatus(request.getStatus() != null ? 
                OpenClass.ClassStatus.valueOf(request.getStatus().toUpperCase()) : OpenClass.ClassStatus.OPEN);
        entity.setVisibilityStatus(OpenClass.VisibilityStatus.PUBLIC);

        OpenClass saved = openClassRepository.save(entity);

        if (request.getDayTimeSlots() != null && !request.getDayTimeSlots().isEmpty()) {
            List<DayTimeSlot> slots = request.getDayTimeSlots().stream()
                    .map(s -> DayTimeSlot.builder()
                            .day(s.getDay()).startTime(s.getStartTime()).endTime(s.getEndTime())
                            .maxStudents(s.getMaxStudents() != null ? s.getMaxStudents() : 10)
                            .bookedCount(0).openClass(saved).build())
                    .toList();
            dayTimeSlotRepository.saveAll(slots);
            saved.setDayTimeSlots(slots);
        }
        return saved;
    }

    @Override
    @Transactional
    public void deleteClass(Long id) {
        OpenClass entity = openClassRepository.findById(id).orElseThrow(() -> new RuntimeException("Class not found"));
        checkOwner(entity);
        long confirmedCount = bookingRepository.countConfirmedByClassId(id);
        if (confirmedCount > 0) {
            throw new RuntimeException("Action Denied: This class has " + confirmedCount + " confirmed student bookings.");
        }
        openClassRepository.delete(entity);
    }

    private OpenClassResponse.DayTimeSlotResponse mapSlot(DayTimeSlot s) {
        return OpenClassResponse.DayTimeSlotResponse.builder()
                .id(s.getId()).day(s.getDay()).startTime(s.getStartTime())
                .endTime(s.getEndTime()).maxStudents(s.getMaxStudents()).bookedCount(s.getBookedCount()).build();
    }

    @Override 
    @Transactional 
    public OpenClassResponse updateClass(Long id, OpenClassRequest request) {
        OpenClass e = openClassRepository.findById(id).orElseThrow();
        checkOwner(e);
        return mapToResponse(save(e, request, e.getTutor()));
    }

    @Override public OpenClassResponse getClassDetails(Long id) { return openClassRepository.findById(id).map(this::mapToResponse).orElseThrow(); }
    @Override public List<OpenClassResponse> findByTutorId(Long tutorId) { return openClassRepository.findByTutorId(tutorId).stream().map(this::mapToResponse).toList(); }
    @Override public List<OpenClassResponse> getAllPublicCards() { return openClassRepository.findAllPublicFeed(OpenClass.ClassStatus.OPEN, OpenClass.VisibilityStatus.PUBLIC).stream().map(this::mapToResponse).toList(); }
    @Override public List<OpenClassResponse> getPublicClassesByTutor(Long tId) { return openClassRepository.findPublicClassesByTutorId(tId, OpenClass.ClassStatus.OPEN, OpenClass.VisibilityStatus.PUBLIC).stream().map(this::mapToResponse).toList(); }
    @Override @Transactional public OpenClassResponse endClass(Long id) { OpenClass e = openClassRepository.findById(id).orElseThrow(); checkOwner(e); e.setStatus(OpenClass.ClassStatus.CLOSED); return mapToResponse(openClassRepository.save(e)); }
    @Override @Transactional public OpenClassResponse reopenClass(Long id) { OpenClass e = openClassRepository.findById(id).orElseThrow(); checkOwner(e); e.setStatus(OpenClass.ClassStatus.OPEN); return mapToResponse(openClassRepository.save(e)); }
    @Override @Transactional(readOnly = true) public List<OpenClassResponse> filterOpenClasses(String loc, String sub) { return openClassRepository.findAll(OpenClassSpecification.filter(loc, sub)).stream().map(this::mapToResponse).toList(); }

    @Override
    @Transactional
    public OpenClassResponse copyClass(Long id) {
        OpenClass original = openClassRepository.findById(id).orElseThrow();
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
        copy.setSubjects(new ArrayList<>(original.getSubjects()));
        copy.setLearningModes(new HashSet<>(original.getLearningModes()));
        copy.setStatus(OpenClass.ClassStatus.OPEN);
        copy.setVisibilityStatus(OpenClass.VisibilityStatus.PUBLIC);

        OpenClass saved = openClassRepository.save(copy);
        if (original.getDayTimeSlots() != null) {
            List<DayTimeSlot> slots = original.getDayTimeSlots().stream()
                    .map(s -> DayTimeSlot.builder().day(s.getDay()).startTime(s.getStartTime()).endTime(s.getEndTime()).maxStudents(s.getMaxStudents()).bookedCount(0).openClass(saved).build())
                    .toList();
            dayTimeSlotRepository.saveAll(slots);
            saved.setDayTimeSlots(slots);
        }
        return mapToResponse(saved);
    }

    private void checkOwner(OpenClass entity) {
        Tutor tutor = getCurrentTutor();
        if (!entity.getTutor().getId().equals(tutor.getId())) throw new RuntimeException("Unauthorized: You do not own this class.");
    }

    private Tutor getCurrentTutor() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return tutorRepository.findByUserEmail(email).orElseThrow(() -> new RuntimeException("Tutor record not found."));
    }
}