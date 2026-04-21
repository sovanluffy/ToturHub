package com.rental_api.ServiceBooking.Services.impl;

import com.rental_api.ServiceBooking.Dto.NotificationMessage;
import com.rental_api.ServiceBooking.Dto.Request.BookingClassRequest;
import com.rental_api.ServiceBooking.Dto.Response.BookingResponse;
import com.rental_api.ServiceBooking.Entity.*;
import com.rental_api.ServiceBooking.Entity.Enum.BookingStatus;
import com.rental_api.ServiceBooking.Exception.ResourceNotFoundException;
import com.rental_api.ServiceBooking.Repository.*;
import com.rental_api.ServiceBooking.Services.BookingService;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final OpenClassRepository openClassRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final DayTimeSlotRepository dayTimeSlotRepository;
    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // ================= NOTIFICATION =================
    private void sendNotification(
            String email,
            String type,
            String content,
            Long bookingId,
            Long classId,
            String studentName,
            String classTitle,
            String day,
            String startTime,
            String endTime,
            String telegram
    ) {

        Notification notification = Notification.builder()
                .recipientEmail(email)
                .type(type)
                .content(content)
                .bookingId(bookingId)
                .classId(classId)
                .isRead(false)
                .build();

        notificationRepository.save(notification);

        messagingTemplate.convertAndSendToUser(
                email,
                "/queue/notifications",
                new NotificationMessage(
                        type,
                        content,
                        bookingId,
                        classId,
                        studentName,
                        classTitle,
                        day,
                        startTime,
                        endTime,
                        telegram
                )
        );
    }

    // ================= BOOK CLASS =================
    @Override
    @Transactional
    public BookingResponse bookClass(Long openClassId, BookingClassRequest request) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User student = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        OpenClass openClass = openClassRepository.findById(openClassId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found"));

        DayTimeSlot slot = dayTimeSlotRepository.findByIdForUpdate(request.getDayTimeSlotId())
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found"));

        if (!slot.getOpenClass().getId().equals(openClassId)) {
            throw new IllegalStateException("Schedule does not belong to this class");
        }

        boolean exists = bookingRepository.existsByUser_IdAndSchedule_Id(
                student.getId(),
                slot.getId()
        );

        if (exists) throw new IllegalStateException("Already booked!");

        int booked = slot.getBookedCount() == null ? 0 : slot.getBookedCount();
        int max = slot.getMaxStudents() == null ? 10 : slot.getMaxStudents();

        if (booked >= max) throw new IllegalStateException("Full slot!");

        slot.setBookedCount(booked + 1);

        BookingClass booking = BookingClass.builder()
                .user(student)
                .tutor(openClass.getTutor())
                .openClass(openClass)
                .schedule(slot)
                .telegram(request.getTelegram())
                .note(request.getNote())
                .status(BookingStatus.PENDING)
                .build();

        BookingClass saved = bookingRepository.save(booking);

        sendNotification(
                openClass.getTutor().getUser().getEmail(),
                "BOOKING_REQUEST",
                student.getFullname() + " requested " + openClass.getTitle(),
                saved.getId(),
                openClass.getId(),
                student.getFullname(),
                openClass.getTitle(),
                slot.getDay().toString(),
                slot.getStartTime().toString(),
                slot.getEndTime().toString(),
                request.getTelegram()
        );

        return mapToResponse(saved);
    }

    // ================= CONFIRM =================
    @Override
    @Transactional
    public BookingResponse confirmBooking(Long bookingId) {
        BookingClass booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        booking.setStatus(BookingStatus.CONFIRMED);
        return mapToResponse(bookingRepository.save(booking));
    }

    // ================= REJECT =================
    @Override
    @Transactional
    public BookingResponse rejectBooking(Long bookingId) {
        BookingClass booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        booking.setStatus(BookingStatus.REJECTED);
        return mapToResponse(bookingRepository.save(booking));
    }

    // ================= REQUIRED METHODS =================
    @Override
    public List<BookingResponse> getBookingsByUserId(Long userId) {
        return bookingRepository.findByUser_Id(userId)
                .stream().map(this::mapToResponse).toList();
    }

    @Override
    public List<BookingResponse> getBookingsByClassId(Long classId) {
        return bookingRepository.findByOpenClass_Id(classId)
                .stream().map(this::mapToResponse).toList();
    }

    @Override
    public List<BookingResponse> getBookingsByTutorId(Long tutorId) {
        return bookingRepository.findByTutor_Id(tutorId)
                .stream().map(this::mapToResponse).toList();
    }

    // ================= CURRENT USER =================
    @Override
    public List<BookingResponse> getMyBookings() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User student = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return bookingRepository.findByUser_Id(student.getId())
                .stream().map(this::mapToResponse).toList();
    }

    @Override
    public List<BookingResponse> getMyTutorBookings() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User tutor = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Tutor not found"));

        return bookingRepository.findByTutor_Id(tutor.getId())
                .stream().map(this::mapToResponse).toList();
    }

    // ================= 🔥 FIXED COUNT (IMPORTANT) =================
    @Override
    public Long getMyPendingBookingsCount() {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User tutor = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Tutor not found"));

        Long count = bookingRepository.countByTutor_IdAndStatus(
                tutor.getId(),
                BookingStatus.PENDING
        );

        // DEBUG (temporary)
        System.out.println("Tutor ID = " + tutor.getId());
        System.out.println("Pending COUNT = " + count);

        return count;
    }

    // ================= MAPPER =================
 private BookingResponse mapToResponse(BookingClass b) {

    return BookingResponse.builder()
            .bookingId(b.getId())

            // USER INFO
            .userId(b.getUser().getId())
            .studentName(b.getUser().getFullname())
            .studentEmail(b.getUser().getEmail())
            .studentPhone(b.getUser().getPhone())
            .studentAvatar(b.getUser().getAvatarUrl())

            // CLASS
            .classId(b.getOpenClass().getId())
            .classTitle(b.getOpenClass().getTitle())

            // SCHEDULE
            .scheduleId(b.getSchedule().getId())
            .day(b.getSchedule().getDay())
            .startTime(b.getSchedule().getStartTime())
            .endTime(b.getSchedule().getEndTime())

            // BOOKING INFO
            .status(b.getStatus())
            .note(b.getNote())
            .telegram(b.getTelegram())
            .createdAt(b.getCreatedAt())

            .build();
}
}