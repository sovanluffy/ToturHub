package com.rental_api.ServiceBooking.Services.impl;

import com.rental_api.ServiceBooking.Dto.Request.BookingClassRequest;
import com.rental_api.ServiceBooking.Dto.Response.BookingResponse;
import com.rental_api.ServiceBooking.Entity.*;
import com.rental_api.ServiceBooking.Entity.Enum.BookingStatus;
import com.rental_api.ServiceBooking.Exception.ResourceNotFoundException;
import com.rental_api.ServiceBooking.Repository.*;
import com.rental_api.ServiceBooking.Services.BookingService;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor 
public class BookingServiceImpl implements BookingService {

    private final OpenClassRepository openClassRepository;
    private final BookingRepository bookingRepository; 
    private final UserRepository userRepository;

    @Override
    @Transactional 
    public BookingResponse bookClass(Long openClassId, BookingClassRequest request) {
        // 1. Get Logged-in Student
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User student = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 2. Find Class & Config
        OpenClass openClass = openClassRepository.findById(openClassId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found"));

        ScheduleConfig selectedConfig = openClass.getSchedules().stream()
                .filter(config -> config.getId().equals(request.getScheduleId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found"));

        // 3. Create the Booking
        BookingClass booking = new BookingClass();
        booking.setUser(student); // Saves to student_id
        booking.setOpenClass(openClass);
        booking.setScheduleConfig(selectedConfig); 
        
        // NEW: Link the Tutor from the OpenClass to the Booking
        booking.setTutor(openClass.getTutor()); // Saves to tutor_id
        
        booking.setStatus(BookingStatus.PENDING);
        booking.setNote(request.getNote());
        
        BookingClass savedBooking = bookingRepository.save(booking);

    // 4. Update Slots
    if (selectedConfig.getIndividualSlots() != null) {
        selectedConfig.getIndividualSlots().forEach(slot -> slot.setBooked(true));
    }

    // 5. Return Response (Fixed Semicolon and Type Casting)
    return new BookingResponse(
            savedBooking.getId(),
            selectedConfig.getId(),
            selectedConfig.getScheduleType(), // No .name() needed if it's a String
            selectedConfig.getStartDate(),
            selectedConfig.getEndDate(),
            selectedConfig.getStartTime(),
            selectedConfig.getEndTime(),
            savedBooking.getStatus(),
            savedBooking.getNote(),
            savedBooking.getCreatedAt() // Ensure DTO is LocalDateTime
    ); // Added missing semicolon
    }
}
