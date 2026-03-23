package com.rental_api.ServiceBooking.Services;

import com.rental_api.ServiceBooking.Entity.User;
import java.util.Optional;

public interface UserService {
    // Retrieves the user's profile based on the email extracted from the JWT
    Optional<User> getUserByEmail(String email);
}