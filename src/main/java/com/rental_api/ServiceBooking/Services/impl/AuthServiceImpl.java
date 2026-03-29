package com.rental_api.ServiceBooking.Services.impl;

import com.rental_api.ServiceBooking.Dto.Request.LoginRequest;
import com.rental_api.ServiceBooking.Dto.Request.RegisterRequest;
import com.rental_api.ServiceBooking.Dto.Response.AuthResponse;
import com.rental_api.ServiceBooking.Entity.Role;
import com.rental_api.ServiceBooking.Entity.User;
import com.rental_api.ServiceBooking.Entity.Tutor;
import com.rental_api.ServiceBooking.Exception.ConflictException;
import com.rental_api.ServiceBooking.Exception.ResourceNotFoundException;
import com.rental_api.ServiceBooking.Repository.RoleRepository;
import com.rental_api.ServiceBooking.Repository.UserRepository;
import com.rental_api.ServiceBooking.Repository.TutorRepository;
import com.rental_api.ServiceBooking.Services.AuthService;
import com.rental_api.ServiceBooking.Services.CloudinaryService;
import com.rental_api.ServiceBooking.Security.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TutorRepository tutorRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final CloudinaryService cloudinaryService;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    // ------------------- REGISTER -------------------
    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request, MultipartFile avatar) {
        validateEmail(request.getEmail());
        
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ConflictException("Email already exists");
        }

        // ✅ Step 1: Upload to Cloudinary
        // This MUST return a string starting with "https://res.cloudinary.com/"
        String avatarUrl = null;
        if (avatar != null && !avatar.isEmpty()) {
            avatarUrl = cloudinaryService.uploadFile(avatar);
            log.info("Cloudinary upload successful. URL: {}", avatarUrl);
        }

        // ✅ Step 2: Build User with the SECURE URL (No local path prefix)
        User user = User.builder()
                .fullname(request.getFullname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .address(request.getAddress())
                .location(request.getLocation())
                .avatarUrl(avatarUrl) 
                .status(User.Status.ACTIVE)
                .build();

        Role studentRole = roleRepository.findByName("student")
                .orElseThrow(() -> new ResourceNotFoundException("Student role not found"));
        user.setRoles(Set.of(studentRole));

        user = userRepository.save(user);
        return buildAuthResponse(user, "User registered successfully with Cloudinary avatar");
    }

    // ------------------- LOGIN -------------------
    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        validateEmail(request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        return buildAuthResponse(user, "Login successful");
    }

    // ------------------- REQUEST TUTOR -------------------
    @Override
    @Transactional
    public AuthResponse requestTutor(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Role tutorRole = roleRepository.findByName("tutor")
                .orElseThrow(() -> new ResourceNotFoundException("Tutor role not found"));

        user.getRoles().add(tutorRole);
        user.setStatus(User.Status.PENDING);
        user = userRepository.save(user);

        // ✅ IMPORTANT: Create Tutor record to link User and Tutor tables
        if (tutorRepository.findByUserEmail(user.getEmail()).isEmpty()) {
            Tutor tutorProfile = Tutor.builder()
                    .user(user)
                    .isPublic(false)
                    .averageRating(0.0)
                    .totalStudentsTaught(0)
                    .yearsOfExperience(0)
                    .build();
            tutorRepository.save(tutorProfile);
        }

        return buildAuthResponse(user, "Tutor request submitted. Awaiting approval.");
    }

    // ------------------- ADMIN APPROVE / REJECT -------------------
    @Override
    @Transactional
    public void approveTutor(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setStatus(User.Status.ACTIVE);
        userRepository.save(user);

        // ✅ FIX: Lombok naming for 'isPublic' field requires 'setPublic'
        tutorRepository.findByUserEmail(user.getEmail()).ifPresent(t -> {
            t.setPublic(true); 
            tutorRepository.save(t);
        });
    }

    @Override
    @Transactional
    public void rejectTutor(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.getRoles().removeIf(r -> r.getName().equalsIgnoreCase("tutor"));
        user.setStatus(User.Status.REJECTED);
        userRepository.save(user);
    }

    // ------------------- HELPERS -------------------
    private void validateEmail(String email) {
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new ConflictException("Invalid email format");
        }
    }

    private AuthResponse buildAuthResponse(User user, String message) {
        List<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());
        
        List<Long> roleIds = user.getRoles().stream()
                .map(Role::getId)
                .collect(Collectors.toList());

        String token = jwtUtils.generateToken(
                user.getId(),
                user.getEmail(),
                user.getEmail(),
                roleNames,
                roleIds
        );

        return AuthResponse.builder()
                .userId(user.getId())
                .fullname(user.getFullname())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl()) // ✅ This will now return the https link
                .message(message)
                .token(token)
                .roles(roleNames)
                .roleIds(roleIds)
                .build();
    }
}