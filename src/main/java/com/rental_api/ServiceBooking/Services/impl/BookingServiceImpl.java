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

        boolean exists = bookingRepository.existsByUserIdAndScheduleId(student.getId(), slot.getId());
        if (exists) {
            throw new IllegalStateException("You already booked this slot!");
        }

        int booked = slot.getBookedCount() == null ? 0 : slot.getBookedCount();
        int max = slot.getMaxStudents() == null ? 10 : slot.getMaxStudents();

        if (booked >= max) {
            throw new IllegalStateException("This schedule is full!");
        }

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
                student.getFullname() + " requested: " + openClass.getTitle(),
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

        BookingClass updated = bookingRepository.save(booking);

        sendNotification(
                booking.getUser().getEmail(),
                "BOOKING_CONFIRMED",
                "Booking confirmed for " + booking.getOpenClass().getTitle(),
                booking.getId(),
                booking.getOpenClass().getId(),
                booking.getUser().getFullname(),
                booking.getOpenClass().getTitle(),
                booking.getSchedule().getDay().toString(),
                booking.getSchedule().getStartTime().toString(),
                booking.getSchedule().getEndTime().toString(),
                booking.getTelegram()
        );

        return mapToResponse(updated);
    }

    // ================= REJECT =================
    @Override
    @Transactional
    public BookingResponse rejectBooking(Long bookingId) {

        BookingClass booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        booking.setStatus(BookingStatus.REJECTED);

        DayTimeSlot slot = booking.getSchedule();
        if (slot != null) {
            int booked = slot.getBookedCount() == null ? 0 : slot.getBookedCount();
            slot.setBookedCount(Math.max(0, booked - 1));
        }

        BookingClass updated = bookingRepository.save(booking);

        sendNotification(
                booking.getUser().getEmail(),
                "BOOKING_REJECTED",
                "Booking rejected for " + booking.getOpenClass().getTitle(),
                booking.getId(),
                booking.getOpenClass().getId(),
                booking.getUser().getFullname(),
                booking.getOpenClass().getTitle(),
                booking.getSchedule().getDay().toString(),
                booking.getSchedule().getStartTime().toString(),
                booking.getSchedule().getEndTime().toString(),
                booking.getTelegram()
        );

        return mapToResponse(updated);
    }

    // ================= USER BOOKINGS =================
    @Override
    public List<BookingResponse> getBookingsByUserId(Long userId) {
        return bookingRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // ================= CLASS BOOKINGS =================
    @Override
    public List<BookingResponse> getBookingsByClassId(Long classId) {
        return bookingRepository.findByOpenClassId(classId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // ================= TUTOR BOOKINGS =================
    @Override
    public List<BookingResponse> getBookingsByTutorId(Long tutorId) {
        return bookingRepository.findByTutorId(tutorId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // ================= STUDENT (ME) =================
    @Override
    public List<BookingResponse> getMyBookings() {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User student = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return bookingRepository.findByUserId(student.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // ================= TUTOR (ME) =================
    @Override
    public List<BookingResponse> getMyTutorBookings() {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User tutor = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Tutor not found"));

        return bookingRepository.findByTutorId(tutor.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // ================= MAPPER =================
    private BookingResponse mapToResponse(BookingClass b) {

        DayTimeSlot s = b.getSchedule();
        OpenClass oc = b.getOpenClass();
        User u = b.getUser();

        return BookingResponse.builder()
                .bookingId(b.getId())
                .userId(u != null ? u.getId() : null)
                .classId(oc != null ? oc.getId() : null)
                .classTitle(oc != null ? oc.getTitle() : null)
                .scheduleId(s != null ? s.getId() : null)
                .day(s != null ? s.getDay() : null)
                .startTime(s != null ? s.getStartTime() : null)
                .endTime(s != null ? s.getEndTime() : null)
                .status(b.getStatus())
                .note(b.getNote())
                .telegram(b.getTelegram())
                .createdAt(b.getCreatedAt())
                .build();
    }
}