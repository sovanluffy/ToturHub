package com.rental_api.ServiceBooking.Services.impl;

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
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional 
    public BookingResponse bookClass(Long openClassId, BookingClassRequest request) {
        // 1. Identify the student from the Security Context (JWT Principal)
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User student = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        // 2. Load the target Class and the specific Schedule Config
        OpenClass openClass = openClassRepository.findById(openClassId)
                .orElseThrow(() -> new ResourceNotFoundException("Open Class not found with ID: " + openClassId));

        ScheduleConfig selectedConfig = openClass.getSchedules().stream()
                .filter(config -> config.getId().equals(request.getScheduleId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Schedule slot not found"));

        // 3. Create the Booking entry
        BookingClass booking = new BookingClass();
        booking.setUser(student); 
        booking.setOpenClass(openClass);
        booking.setScheduleConfig(selectedConfig); 
        booking.setTutor(openClass.getTutor()); // Link tutor from the class
        booking.setTelegram(request.getTelegram()); 
        booking.setStatus(BookingStatus.PENDING);
        booking.setNote(request.getNote());
        
        BookingClass savedBooking = bookingRepository.save(booking);

        // 4. Update Slot Availability (Logic for individual slots)
        if (selectedConfig.getIndividualSlots() != null) {
            selectedConfig.getIndividualSlots().forEach(slot -> slot.setBooked(true));
        }

        // 5. NOTIFY TUTOR: Send real-time alert to Tutor's email
        messagingTemplate.convertAndSendToUser(
            openClass.getTutor().getUser().getEmail(), 
            "/queue/notifications", 
            "New booking request from " + student.getFullname() + " for: " + openClass.getTitle()
        );

        return mapToResponse(savedBooking, selectedConfig);
    }

    @Override
    @Transactional
    public BookingResponse confirmBooking(Long bookingId) {
        BookingClass booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found")); 
        
        booking.setStatus(BookingStatus.CONFIRMED);
        BookingClass updated = bookingRepository.save(booking);

        // NOTIFY STUDENT: Booking Approved
        messagingTemplate.convertAndSendToUser(
            booking.getUser().getEmail(),
            "/queue/notifications",
            "Great news! Your booking for " + booking.getOpenClass().getTitle() + " has been CONFIRMED."
        );

        return mapToResponse(updated, updated.getScheduleConfig());
    }

    @Override
    @Transactional
    public BookingResponse rejectBooking(Long bookingId) {
        BookingClass booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        
        booking.setStatus(BookingStatus.REJECTED);
        BookingClass updated = bookingRepository.save(booking);

        // NOTIFY STUDENT: Booking Rejected
        messagingTemplate.convertAndSendToUser(
            booking.getUser().getEmail(),
            "/queue/notifications",
            "Update: Your booking for " + booking.getOpenClass().getTitle() + " was not accepted."
        );

        return mapToResponse(updated, updated.getScheduleConfig());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByClassId(Long openClassId) {
        return bookingRepository.findByOpenClassId(openClassId).stream()
                .map(b -> mapToResponse(b, b.getScheduleConfig()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByUserId(Long userId) {
        return bookingRepository.findByUserId(userId).stream()
                .map(b -> mapToResponse(b, b.getScheduleConfig()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByTutorId(Long tutorId) {
        return bookingRepository.findByTutorId(tutorId).stream()
                .map(b -> mapToResponse(b, b.getScheduleConfig()))
                .collect(Collectors.toList());
    }

    /**
     * Helper method to map Entity to DTO clear
     */
    private BookingResponse mapToResponse(BookingClass b, ScheduleConfig s) {
        return new BookingResponse(
                b.getId(), 
                s.getId(), 
                s.getScheduleType(),
                s.getStartDate(), 
                s.getEndDate(), 
                s.getStartTime(), 
                s.getEndTime(),
                b.getStatus(), 
                b.getNote(), 
                b.getTelegram(), 
                b.getCreatedAt()
        );
    }
}