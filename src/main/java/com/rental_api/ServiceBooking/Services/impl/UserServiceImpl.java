package com.rental_api.ServiceBooking.Services.impl;

import com.rental_api.ServiceBooking.Entity.User;
import com.rental_api.ServiceBooking.Repository.UserRepository;
import com.rental_api.ServiceBooking.Services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public Optional<User> getUserByEmail(String email) {
        // Returns the User entity including roles (due to EAGER fetch or @EntityGraph)
        return userRepository.findByEmail(email);
    }
}