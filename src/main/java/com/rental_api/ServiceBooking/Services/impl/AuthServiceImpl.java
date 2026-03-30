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
    private final CloudinaryService cloudinaryService; // This will now use your CloudinaryServiceImpl

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request, MultipartFile avatar) {
        validateEmail(request.getEmail());

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ConflictException("Email already exists");
        }

        // 1. Upload avatar to Cloudinary
        String avatarUrl = null;
        if (avatar != null && !avatar.isEmpty()) {
            try {
                // This call goes to the cloud and returns a secure HTTPS URL
                avatarUrl = cloudinaryService.uploadFile(avatar);
                log.info("Avatar successfully hosted on Cloudinary: {}", avatarUrl);
            } catch (Exception e) {
                log.error("Cloudinary upload failed during registration", e);
                throw new RuntimeException("Could not process avatar upload");
            }
        }

        // 2. Create User Entity with the Cloud URL
        User user = User.builder()
                .fullname(request.getFullname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .address(request.getAddress())
                .location(request.getLocation())
                .avatarUrl(avatarUrl) // Saving the https:// link here
                .status(User.Status.ACTIVE)
                .build();

        // Assign default role
        Role studentRole = roleRepository.findByName("student")
                .orElseThrow(() -> new ResourceNotFoundException("Student role not found"));
        user.setRoles(Set.of(studentRole));

        user = userRepository.save(user);

        return buildAuthResponse(user, "User registered successfully with Cloudinary storage");
    }

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

    // --- OTHER METHODS (Tutor Request, etc.) ---
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

        return buildAuthResponse(user, "Tutor request submitted.");
    }

    @Override @Transactional public void approveTutor(Long userId) { /* Logic */ }
    @Override @Transactional public void rejectTutor(Long userId) { /* Logic */ }

    // --- HELPERS ---
    private void validateEmail(String email) {
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new ConflictException("Invalid email format");
        }
    }

    private AuthResponse buildAuthResponse(User user, String message) {
        List<String> roleNames = user.getRoles().stream().map(Role::getName).collect(Collectors.toList());
        List<Long> roleIds = user.getRoles().stream().map(Role::getId).collect(Collectors.toList());

        String token = jwtUtils.generateToken(user.getId(), user.getEmail(), user.getEmail(), roleNames, roleIds);

        return AuthResponse.builder()
                .userId(user.getId())
                .fullname(user.getFullname())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl()) 
                .message(message)
                .token(token)
                .roles(roleNames)
                .roleIds(roleIds)
                .build();
    }
}