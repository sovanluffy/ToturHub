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
    private final DayTimeSlotRepository dayTimeSlotRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // ================= BOOK CLASS =================
    @Override
    @Transactional
    public BookingResponse bookClass(Long openClassId, BookingClassRequest request) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User student = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        OpenClass openClass = openClassRepository.findById(openClassId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found"));

        DayTimeSlot slot = dayTimeSlotRepository.findById(request.getDayTimeSlotId())
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found"));

        // validate slot belongs to class
        if (!slot.getOpenClass().getId().equals(openClassId)) {
            throw new RuntimeException("Schedule does not belong to this class");
        }

        // check booking
        if (Boolean.TRUE.equals(slot.getBooked())) {
            throw new RuntimeException("This schedule is already booked!");
        }

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

        // mark slot booked
        slot.setBooked(true);
        dayTimeSlotRepository.save(slot);

        // notify tutor
        messagingTemplate.convertAndSendToUser(
                openClass.getTutor().getUser().getEmail(),
                "/queue/notifications",
                new NotificationMessage(
                        "BOOKING_REQUEST",
                        student.getFullname() + " booked your class: " + openClass.getTitle(),
                        saved.getId(),
                        openClass.getId()
                )
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

        messagingTemplate.convertAndSendToUser(
                booking.getUser().getEmail(),
                "/queue/notifications",
                new NotificationMessage(
                        "BOOKING_CONFIRMED",
                        "Booking confirmed: " + booking.getOpenClass().getTitle(),
                        booking.getId(),
                        booking.getOpenClass().getId()
                )
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
            slot.setBooked(false);
            dayTimeSlotRepository.save(slot);
        }

        BookingClass updated = bookingRepository.save(booking);

        messagingTemplate.convertAndSendToUser(
                booking.getUser().getEmail(),
                "/queue/notifications",
                new NotificationMessage(
                        "BOOKING_REJECTED",
                        "Booking rejected: " + booking.getOpenClass().getTitle(),
                        booking.getId(),
                        booking.getOpenClass().getId()
                )
        );

        return mapToResponse(updated);
    }

    // ================= GET =================
    @Override
    public List<BookingResponse> getBookingsByUserId(Long userId) {
        return bookingRepository.findByUserId(userId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public List<BookingResponse> getBookingsByClassId(Long classId) {
        return bookingRepository.findByOpenClassId(classId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public List<BookingResponse> getBookingsByTutorId(Long tutorId) {
        return bookingRepository.findByTutorId(tutorId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // ================= MAPPER =================
    private BookingResponse mapToResponse(BookingClass b) {

        DayTimeSlot s = b.getSchedule();

        return BookingResponse.builder()
                .bookingId(b.getId())
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