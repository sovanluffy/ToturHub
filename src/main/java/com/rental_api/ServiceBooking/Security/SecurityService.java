package com.rental_api.ServiceBooking.Security;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.rental_api.ServiceBooking.Repository.BookingRepository;
import com.rental_api.ServiceBooking.Repository.OpenClassRepository;
import com.rental_api.ServiceBooking.Repository.UserRepository;

import lombok.RequiredArgsConstructor;


@Component("securityService")
@RequiredArgsConstructor
public class SecurityService {
    private final OpenClassRepository openClassRepository ;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

   public boolean isTutorOfClass(Authentication authentication, Long openClassId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String currentUserEmail = authentication.getName();

        return openClassRepository.findById(openClassId)
                .map(openClass -> openClass.getTutor().getUser().getEmail().equals(currentUserEmail))
                .orElse(false);
    }


    public boolean isSelf(Long userId, String currentUserEmail) {
        return userRepository.findById(userId)
                .map(user -> user.getEmail().equals(currentUserEmail))
                .orElse(false);
    }

    public boolean isTutorOfBooking(Authentication authentication, Long bookingId) {
    String currentUserEmail = authentication.getName();
    
    // Find the booking, then check the tutor of the associated class
    return bookingRepository.findById(bookingId)
            .map(booking -> booking.getOpenClass().getTutor().getUser().getEmail().equals(currentUserEmail))
            .orElse(false);
    }
}
