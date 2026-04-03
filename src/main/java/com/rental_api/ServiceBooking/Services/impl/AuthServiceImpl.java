package com.rental_api.ServiceBooking.Services.impl;

import com.rental_api.ServiceBooking.Dto.Request.LoginRequest;
import com.rental_api.ServiceBooking.Dto.Request.RegisterRequest;
import com.rental_api.ServiceBooking.Dto.Response.AuthResponse;
import com.rental_api.ServiceBooking.Dto.Response.ProfileResponse;
import com.rental_api.ServiceBooking.Entity.*;
import com.rental_api.ServiceBooking.Exception.ConflictException;
import com.rental_api.ServiceBooking.Exception.ResourceNotFoundException;
import com.rental_api.ServiceBooking.Repository.*;
import com.rental_api.ServiceBooking.Services.AuthService;
import com.rental_api.ServiceBooking.Services.CloudinaryService;
import com.rental_api.ServiceBooking.Security.CustomUserDetails;
import com.rental_api.ServiceBooking.Security.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final LocationRepository locationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final CloudinaryService cloudinaryService;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    // -----------------------------
    // REGISTER
    // -----------------------------
    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request, MultipartFile avatar) {
        validateEmail(request.getEmail());

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ConflictException("Email already exists");
        }

        // Upload avatar if provided
        String avatarUrl = null;
        if (avatar != null && !avatar.isEmpty()) {
            avatarUrl = cloudinaryService.uploadFile(avatar);
        }

        // Validate location exists if provided
        if (request.getLocationId() != null) {
            locationRepository.findById(request.getLocationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Location not found: " + request.getLocationId()));
        }

        // Create User
        User user = User.builder()
                .fullname(request.getFullname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .address(request.getAddress())
                .avatarUrl(avatarUrl)
                .status(User.Status.ACTIVE)
                .locationId(request.getLocationId())
                .build();

        // Assign default student role
        Role studentRole = roleRepository.findByName("student")
                .orElseThrow(() -> new ResourceNotFoundException("Student role not found"));
        user.setRoles(Set.of(studentRole));

        user = userRepository.save(user);

        return buildAuthResponse(user, "User registered successfully");
    }

    // -----------------------------
    // LOGIN
    // -----------------------------
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

    // -----------------------------
    // GET PROFILE BY USER ID (Admin Use)
    // -----------------------------
    @Override
    @Transactional(readOnly = true)
    public ProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return buildProfileResponse(user);
    }

    // -----------------------------
    // GET PROFILE FROM TOKEN (Current Logged-in User)
    // -----------------------------
    @Override
    @Transactional(readOnly = true)
    public ProfileResponse getProfileFromToken() {
        Long userId = getCurrentUserId();
        return getProfile(userId); // reuse existing method
    }

    // -----------------------------
    // REQUEST TUTOR
    // -----------------------------
    @Override
    @Transactional
    public AuthResponse requestTutor() {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getStatus() == User.Status.PENDING) {
            throw new ConflictException("Tutor request already submitted");
        }

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

        return buildAuthResponse(user, "Tutor request submitted");
    }

    // -----------------------------
    // APPROVE / REJECT TUTOR
    // -----------------------------
    @Override
    @Transactional
    public void approveTutor(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setStatus(User.Status.ACTIVE);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void rejectTutor(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setStatus(User.Status.REJECTED);
        userRepository.save(user);
    }

    // -----------------------------
    // HELPERS
    // -----------------------------
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
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .message(message)
                .token(token)
                .roles(roleNames)
                .roleIds(roleIds)
                .locationId(user.getLocationId())
                .city(user.getLocationId() != null ? locationRepository.findById(user.getLocationId()).map(Location::getCity).orElse(null) : null)
                .district(user.getLocationId() != null ? locationRepository.findById(user.getLocationId()).map(Location::getDistrict).orElse(null) : null)
                .fullAddress(user.getLocationId() != null ? locationRepository.findById(user.getLocationId()).map(Location::getAddress).orElse(null) : null)
                .build();
    }

    private ProfileResponse buildProfileResponse(User user) {
        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        Location location = null;
        if (user.getLocationId() != null) {
            location = locationRepository.findById(user.getLocationId()).orElse(null);
        }

        return ProfileResponse.builder()
                .userId(user.getId())
                .fullname(user.getFullname())
                .email(user.getEmail())
                .phone(user.getPhone())
                .address(user.getAddress())
                .avatarUrl(user.getAvatarUrl())
                .status(user.getStatus().name())
                .roles(roles)
                .locationId(user.getLocationId())
                .city(location != null ? location.getCity() : null)
                .district(location != null ? location.getDistrict() : null)
                .fullAddress(location != null ? location.getAddress() : null)
                .build();
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() &&
                authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails.getId();
        }
        throw new ResourceNotFoundException("Authenticated user not found");
    }
}