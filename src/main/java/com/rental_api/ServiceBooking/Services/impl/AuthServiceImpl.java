package com.rental_api.ServiceBooking.Services.impl;

import com.rental_api.ServiceBooking.Dto.Request.LoginRequest;
import com.rental_api.ServiceBooking.Dto.Request.RegisterRequest;
import com.rental_api.ServiceBooking.Dto.Response.AuthResponse;
import com.rental_api.ServiceBooking.Dto.Response.ProfileResponse;
import com.rental_api.ServiceBooking.Entity.*;
import com.rental_api.ServiceBooking.Exception.ConflictException;
import com.rental_api.ServiceBooking.Exception.ResourceNotFoundException;
import com.rental_api.ServiceBooking.Repository.*;
import com.rental_api.ServiceBooking.Security.JwtUtils;
import com.rental_api.ServiceBooking.Services.AuthService;
import com.rental_api.ServiceBooking.Services.CloudinaryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

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

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    // ================= REGISTER =================
    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request, MultipartFile avatar) {

        validateEmail(request.getEmail());

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ConflictException("Email already exists");
        }

        String avatarUrl = null;
        if (avatar != null && !avatar.isEmpty()) {
            avatarUrl = cloudinaryService.uploadFile(avatar);
        }

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

        Role studentRole = roleRepository.findByName("STUDENT")
                .orElseThrow(() -> new ResourceNotFoundException("Student role not found"));

        user.setRoles(Set.of(studentRole));

        user = userRepository.save(user);

        return buildAuthResponse(user, "User registered successfully");
    }

    // ================= LOGIN =================
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

    // ================= PROFILE =================
    @Override
    @Transactional(readOnly = true)
    public ProfileResponse getProfile(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return buildProfileResponse(user);
    }

    @Override
    public ProfileResponse getProfileFromToken() {
        Long userId = getCurrentUserId();
        return getProfile(userId);
    }

    // ================= REQUEST TUTOR =================
    @Override
    @Transactional
    public AuthResponse requestTutor() {

        Long userId = getCurrentUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Role tutorRole = roleRepository.findByName("TUTOR")
                .orElseThrow(() -> new ResourceNotFoundException("Tutor role not found"));

        user.getRoles().add(tutorRole);
        user.setStatus(User.Status.PENDING);

        userRepository.save(user);

        if (tutorRepository.findByUserId(user.getId()).isEmpty()) {
            Tutor tutor = Tutor.builder()
                    .user(user)
                    .isPublic(false)
                    .averageRating(0.0)
                    .totalStudentsTaught(0)
                    .yearsOfExperience(0)
                    .build();

            tutorRepository.save(tutor);
        }

        return buildAuthResponse(user, "Tutor request submitted");
    }

    // ================= APPROVE / REJECT =================
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

    // ================= BUILD AUTH RESPONSE =================
    private AuthResponse buildAuthResponse(User user, String message) {

        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .toList();

        List<Long> roleIds = user.getRoles().stream()
                .map(Role::getId)
                .toList();

        // 🔥 GET tutorId from DB
        Long tutorId = null;

        if (roles.contains("TUTOR")) {
            tutorId = tutorRepository.findByUserId(user.getId())
                    .map(Tutor::getId)
                    .orElse(null);
        }

        // ⚠️ IMPORTANT FIX: ORDER MUST MATCH JwtUtils
        String token = jwtUtils.generateToken(
                user.getId(),        // userId
                tutorId,             // tutorId
                user.getEmail(),     // email
                user.getFullname(),  // username
                roles,
                roleIds
        );

        return AuthResponse.builder()
                .userId(user.getId())
                .fullname(user.getFullname())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .roles(roles)
                .roleIds(roleIds)
                .token(token)
                .message(message)
                .locationId(user.getLocationId())
                .build();
    }

    // ================= PROFILE =================
    private ProfileResponse buildProfileResponse(User user) {

        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .toList();

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

    // ================= SECURITY =================
    private Long getCurrentUserId() {
        return (Long) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
    }

    private void validateEmail(String email) {
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new ConflictException("Invalid email format");
        }
    }
}