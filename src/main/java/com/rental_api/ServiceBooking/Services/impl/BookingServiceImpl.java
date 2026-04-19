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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final OpenClassRepository openClassRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ClassScheduleRepository classScheduleRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // ===================== BOOK CLASS =====================
    @Override
    @Transactional
    public BookingResponse bookClass(Long openClassId, BookingClassRequest request) {

        // 1. Get current user (student)
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        User student = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        // 2. Get class
        OpenClass openClass = openClassRepository.findById(openClassId)
                .orElseThrow(() -> new ResourceNotFoundException("Open class not found with ID: " + openClassId));

        // 3. Get selected slot
        ClassSchedule slot = classScheduleRepository.findById(request.getScheduleId())
                .orElseThrow(() -> new ResourceNotFoundException("Time slot not found"));

        if (slot.isBooked()) {
            throw new RuntimeException("This slot is already booked!");
        }

        // 4. Create booking
        BookingClass booking = new BookingClass();
        booking.setUser(student);
        booking.setTutor(openClass.getTutor());
        booking.setOpenClass(openClass);
        booking.setScheduleConfig(slot.getConfig());
        booking.setTelegram(request.getTelegram());
        booking.setNote(request.getNote());
        booking.setStatus(BookingStatus.PENDING);

        BookingClass savedBooking = bookingRepository.save(booking);

        // 5. Mark slot as booked
        slot.setBooked(true);
        classScheduleRepository.save(slot);

        // 6. 🔥 SEND NOTIFICATION TO TUTOR
        NotificationMessage notification = new NotificationMessage(
                "BOOKING_REQUEST",
                student.getFullname() + " booked your class: " + openClass.getTitle(),
                savedBooking.getId(),
                openClass.getId());

        messagingTemplate.convertAndSendToUser(
                openClass.getTutor().getUser().getEmail(),
                "/queue/notifications",
                notification);

        return mapToResponse(savedBooking, savedBooking.getScheduleConfig());
    }

    // ===================== CONFIRM BOOKING =====================
    @Override
    @Transactional
    public BookingResponse confirmBooking(Long bookingId) {

        BookingClass booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        booking.setStatus(BookingStatus.CONFIRMED);
        BookingClass updated = bookingRepository.save(booking);

        // 🔥 SEND NOTIFICATION TO STUDENT
        NotificationMessage notification = new NotificationMessage(
                "BOOKING_CONFIRMED",
                "Your booking for '" + booking.getOpenClass().getTitle() + "' is CONFIRMED",
                booking.getId(),
                booking.getOpenClass().getId());

        messagingTemplate.convertAndSendToUser(
                booking.getUser().getEmail(),
                "/queue/notifications",
                notification);

        return mapToResponse(updated, updated.getScheduleConfig());
    }

    // ===================== REJECT BOOKING =====================
    @Override
    @Transactional
    public BookingResponse rejectBooking(Long bookingId) {

        BookingClass booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        booking.setStatus(BookingStatus.REJECTED);

        // Make slot available again
        if (booking.getScheduleConfig() != null &&
                booking.getScheduleConfig().getIndividualSlots() != null) {

            booking.getScheduleConfig()
                    .getIndividualSlots()
                    .forEach(slot -> slot.setBooked(false));
        }

        BookingClass updated = bookingRepository.save(booking);

        // 🔥 SEND NOTIFICATION TO STUDENT
        NotificationMessage notification = new NotificationMessage(
                "BOOKING_REJECTED",
                "Your booking for '" + booking.getOpenClass().getTitle() + "' was rejected",
                booking.getId(),
                booking.getOpenClass().getId());

        messagingTemplate.convertAndSendToUser(
                booking.getUser().getEmail(),
                "/queue/notifications",
                notification);

        return mapToResponse(updated, updated.getScheduleConfig());
    }

    // ===================== GET BY CLASS =====================
    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByClassId(Long openClassId) {
        return bookingRepository.findByOpenClassId(openClassId)
                .stream()
                .map(b -> mapToResponse(b, b.getScheduleConfig()))
                .collect(Collectors.toList());
    }

    // ===================== GET BY USER =====================
    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByUserId(Long userId) {
        return bookingRepository.findByUserId(userId)
                .stream()
                .map(b -> mapToResponse(b, b.getScheduleConfig()))
                .collect(Collectors.toList());
    }

    // ===================== GET BY TUTOR =====================
    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByTutorId(Long tutorId) {
        return bookingRepository.findByTutorId(tutorId)
                .stream()
                .map(b -> mapToResponse(b, b.getScheduleConfig()))
                .collect(Collectors.toList());
    }

    // ===================== MAPPER =====================
    private BookingResponse mapToResponse(BookingClass b, ScheduleConfig s) {

        return new BookingResponse(
                b.getId(),
                s != null ? s.getId() : null,
                s != null ? s.getScheduleType() : null,
                s != null ? s.getStartDate() : null,
                s != null ? s.getEndDate() : null,
                s != null ? s.getStartTime() : null,
                s != null ? s.getEndTime() : null,
                b.getStatus(),
                b.getNote(),
                b.getTelegram(),
                b.getCreatedAt());
    }
}