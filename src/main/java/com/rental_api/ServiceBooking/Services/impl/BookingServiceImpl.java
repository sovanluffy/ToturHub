package com.rental_api.ServiceBooking.Services.impl;

import com.rental_api.ServiceBooking.Dto.Request.bookingClassRequest;
import com.rental_api.ServiceBooking.Dto.Response.BookingResponse;
import com.rental_api.ServiceBooking.Entity.*;
import com.rental_api.ServiceBooking.Entity.Enum.BookingStatus;
import com.rental_api.ServiceBooking.Repository.*;
import com.rental_api.ServiceBooking.Services.BookingService;
import com.rental_api.ServiceBooking.Security.JwtUtils; 
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor 
public class BookingServiceImpl implements BookingService {

    private final OpenClassRepository openClassRepository;
    private final BookingRepository bookingRepository; 
    private final UserRepository userRepository;
    private final JwtUtils jwtUtils; 

    @Override
    @Transactional 
    public BookingResponse bookClass(Long openClassId, bookingClassRequest request, String authToken) {
        
        // 1. Auth Validation
        if (authToken == null || !authToken.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid token");
        }
        String token = authToken.substring(7);
        String email = jwtUtils.extractEmail(token); 
        User student = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Find Class
        OpenClass openClass = openClassRepository.findById(openClassId)
                .orElseThrow(() -> new RuntimeException("Class not found"));

        // 3. Find Schedule Config (The "Rule")
        // Note: Make sure OpenClass imports the Entity version of scheduleConfig!
        ScheduleConfig selectedConfig = openClass.getSchedules().stream()
                .filter(config -> config.getId().equals(request.getScheduleId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Schedule not found"));

        // 4. Save Booking
        BookingClass booking = new BookingClass();
        booking.setUser(student);
        booking.setOpenClass(openClass);
        booking.setScheduleConfig(selectedConfig); 
        booking.setStatus(BookingStatus.PENDING);
        booking.setNote(request.getNote());
        
        BookingClass savedBooking = bookingRepository.save(booking);

        // 5. Mark Slots as Booked
        if (selectedConfig.getIndividualSlots() != null) {
            selectedConfig.getIndividualSlots().forEach(slot -> slot.setBooked(true));
        }

        // 6. Map Response
        BookingResponse response = new BookingResponse();
        response.setId(savedBooking.getId());
        response.setScheduleType(selectedConfig.getScheduleType());
        response.setStartDate(selectedConfig.getStartDate());
        response.setEndDate(selectedConfig.getEndDate());
        response.setStartTime(selectedConfig.getStartTime());
        response.setEndTime(selectedConfig.getEndTime());
        response.setStatus(savedBooking.getStatus());
        
        return response;
    }
}