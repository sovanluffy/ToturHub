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
    private final ClassScheduleRepository classScheduleRepository; // ✅ Added this
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional 
    public BookingResponse bookClass(Long openClassId, BookingClassRequest request) {
        // 1. Identify the student
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User student = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        // 2. Load the Class
        OpenClass openClass = openClassRepository.findById(openClassId)
                .orElseThrow(() -> new ResourceNotFoundException("Open Class not found with ID: " + openClassId));

        // 3. Find the specific slot being booked (ClassSchedule)
        // We now book based on the specific slot ID sent from the frontend
        ClassSchedule selectedSlot = classScheduleRepository.findById(request.getScheduleId())
                .orElseThrow(() -> new ResourceNotFoundException("Specific time slot not found"));

        if (selectedSlot.isBooked()) {
            throw new RuntimeException("This time slot is already booked!");
        }

        // 4. Create the Booking entry
        BookingClass booking = new BookingClass();
        booking.setUser(student); 
        booking.setOpenClass(openClass);
        booking.setScheduleConfig(selectedSlot.getConfig()); // Link to parent config
        booking.setTutor(openClass.getTutor());
        booking.setTelegram(request.getTelegram()); 
        booking.setStatus(BookingStatus.PENDING);
        booking.setNote(request.getNote());
        
        BookingClass savedBooking = bookingRepository.save(booking);

        // 5. Update Slot Availability
        // ✅ Uses the field that matches your 'is_booked' column in the database
        selectedSlot.setBooked(true);
        classScheduleRepository.save(selectedSlot);

        // 6. NOTIFY TUTOR
        messagingTemplate.convertAndSendToUser(
            openClass.getTutor().getUser().getEmail(), 
            "/queue/notifications", 
            "New booking request from " + student.getFullname() + " for: " + openClass.getTitle()
        );

        return mapToResponse(savedBooking, selectedSlot.getConfig());
    }

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
        
        // ✅ If rejected, we should make the slot available again
        // Note: This assumes 1 booking = 1 slot. 
        // If your ScheduleConfig contains multiple slots, you'd loop them here.
        if (booking.getScheduleConfig() != null && booking.getScheduleConfig().getIndividualSlots() != null) {
            booking.getScheduleConfig().getIndividualSlots().forEach(slot -> slot.setBooked(false));
        }

        BookingClass updated = bookingRepository.save(booking);

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

    private BookingResponse mapToResponse(BookingClass b, ScheduleConfig s) {
        // Handle potential null config safely
        Long configId = (s != null) ? s.getId() : null;
        String type = (s != null) ? s.getScheduleType() : "N/A";
        
        return new BookingResponse(
                b.getId(), 
                configId, 
                type,
                (s != null ? s.getStartDate() : null), 
                (s != null ? s.getEndDate() : null), 
                (s != null ? s.getStartTime() : null), 
                (s != null ? s.getEndTime() : null),
                b.getStatus(), 
                b.getNote(), 
                b.getTelegram(), 
                b.getCreatedAt()
        );
    }
}