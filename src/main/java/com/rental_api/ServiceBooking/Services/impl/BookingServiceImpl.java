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

        // ================= BOOK CLASS =================
        @Override
        @Transactional
        public BookingResponse bookClass(Long openClassId, BookingClassRequest request) {

                String email = SecurityContextHolder.getContext().getAuthentication().getName();

                User student = userRepository.findByEmail(email)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                OpenClass openClass = openClassRepository.findById(openClassId)
                                .orElseThrow(() -> new ResourceNotFoundException("Class not found"));

                ClassSchedule slot = classScheduleRepository.findById(request.getScheduleId())
                                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found"));

                // ================= CHECK SLOT =================
                if (slot.isBooked()) {
                        throw new RuntimeException("This schedule is already booked!");
                }

                // ================= CREATE BOOKING =================
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

                // ================= MARK SLOT BOOKED =================
                slot.setBooked(true);
                classScheduleRepository.save(slot);

                // ================= NOTIFY TUTOR =================
                String tutorEmail = openClass.getTutor().getUser().getEmail();

                messagingTemplate.convertAndSendToUser(
                                tutorEmail,
                                "/queue/notifications",
                                new NotificationMessage(
                                                "BOOKING_REQUEST",
                                                student.getFullname() + " booked your class: " + openClass.getTitle(),
                                                saved.getId(),
                                                openClass.getId()));

                return mapToResponse(saved);
        }

        // ================= CONFIRM BOOKING =================
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
                                                booking.getOpenClass().getId()));

                return mapToResponse(updated);
        }

        // ================= REJECT BOOKING =================
        @Override
        @Transactional
        public BookingResponse rejectBooking(Long bookingId) {

                BookingClass booking = bookingRepository.findById(bookingId)
                                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

                booking.setStatus(BookingStatus.REJECTED);

                // ================= FREE SLOT =================
                ClassSchedule slot = booking.getSchedule();
                if (slot != null) {
                        slot.setBooked(false);
                        classScheduleRepository.save(slot);
                }

                BookingClass updated = bookingRepository.save(booking);

                messagingTemplate.convertAndSendToUser(
                                booking.getUser().getEmail(),
                                "/queue/notifications",
                                new NotificationMessage(
                                                "BOOKING_REJECTED",
                                                "Booking rejected: " + booking.getOpenClass().getTitle(),
                                                booking.getId(),
                                                booking.getOpenClass().getId()));

                return mapToResponse(updated);
        }

        // ================= GET BOOKINGS =================
        @Override
        public List<BookingResponse> getBookingsByUserId(Long userId) {
                return bookingRepository.findByUserId(userId)
                                .stream()
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());
        }

        @Override
        public List<BookingResponse> getBookingsByClassId(Long classId) {
                return bookingRepository.findByOpenClassId(classId)
                                .stream()
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());
        }

        @Override
        public List<BookingResponse> getBookingsByTutorId(Long tutorId) {
                return bookingRepository.findByTutorId(tutorId)
                                .stream()
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());
        }

        // ================= SAFE MAPPER =================
        private BookingResponse mapToResponse(BookingClass b) {

                ClassSchedule s = b.getSchedule();

                return BookingResponse.builder()
                                .bookingId(b.getId())
                                .scheduleId(s != null ? s.getId() : null)
                                .scheduleType(s != null ? s.getScheduleType() : null)
                                .startDate(s != null ? s.getStartDate() : null)
                                .endDate(s != null ? s.getEndDate() : null)
                                .status(b.getStatus())
                                .note(b.getNote())
                                .telegram(b.getTelegram())
                                .createdAt(b.getCreatedAt())
                                .build();
        }
}