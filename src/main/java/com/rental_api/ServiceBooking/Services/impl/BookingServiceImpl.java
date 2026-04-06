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

import java.util.List;
import java.util.stream.Collectors;

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

        // 3. CAPACITY CHECK (NEW)
        // Count confirmed bookings for this specific class/schedule
        long confirmedBookings = bookingRepository.countByOpenClassIdAndStatus(openClassId, BookingStatus.CONFIRMED);
        
        if (confirmedBookings >= openClass.getMaxStudents()) {
            throw new RuntimeException("This class is already full! (Max: " + openClass.getMaxStudents() + ")");
        }

        // 4. Create the Booking
        BookingClass booking = new BookingClass();
        booking.setUser(student); 
        booking.setOpenClass(openClass);
        booking.setScheduleConfig(selectedConfig); 
        booking.setTelegram(request.getTelegram()); 
        booking.setTutor(openClass.getTutor()); 
        booking.setStatus(BookingStatus.PENDING); // Start as Pending
        booking.setNote(request.getNote());
        
        BookingClass savedBooking = bookingRepository.save(booking);

        // 5. REMOVED: slot.setBooked(true) 
        // We do not setBooked(true) anymore because we want other students to be able to book too!

        return mapToResponse(savedBooking);
    }

    @Override
    @Transactional
    public BookingResponse conformBooking(Long bookingId) {
        BookingClass booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found")); 
        
        // Final check before confirming: Is there still room?
        long confirmedCount = bookingRepository.countByOpenClassIdAndStatus(
                booking.getOpenClass().getId(), BookingStatus.CONFIRMED);
        
        if (confirmedCount >= booking.getOpenClass().getMaxStudents()) {
            throw new RuntimeException("Cannot confirm: Class capacity reached.");
        }

        booking.setStatus(BookingStatus.CONFIRMED);
        return mapToResponse(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingResponse rejectBooking(Long bookingId) {
        BookingClass booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        booking.setStatus(BookingStatus.REJECTED);
        return mapToResponse(bookingRepository.save(booking));
    }

    // Helper method to keep code clean
    private BookingResponse mapToResponse(BookingClass b) {
        ScheduleConfig sc = b.getScheduleConfig();
        return new BookingResponse(
                b.getId(),
                sc.getId(),
                sc.getScheduleType(),
                sc.getStartDate(),
                sc.getEndDate(),
                sc.getStartTime(),
                sc.getEndTime(),
                b.getStatus(),
                b.getNote(),
                b.getTelegram(),
                b.getCreatedAt()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByClassId(Long openClassId) {
        return bookingRepository.findByOpenClassId(openClassId).stream()
                .map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByUserId(Long userId) {
        return bookingRepository.findByUserId(userId).stream()
                .map(this::mapToResponse).collect(Collectors.toList());
    }
}