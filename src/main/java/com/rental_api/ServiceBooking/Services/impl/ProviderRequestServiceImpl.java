package com.rental_api.ServiceBooking.Services.impl;

import com.rental_api.ServiceBooking.Dto.Request.ProviderRequestDto;
import com.rental_api.ServiceBooking.Dto.Response.ProviderRequestResponse;
import com.rental_api.ServiceBooking.Dto.Response.UserInfoResponse;
import com.rental_api.ServiceBooking.Entity.ProviderRequest;
import com.rental_api.ServiceBooking.Entity.Role;
import com.rental_api.ServiceBooking.Entity.ServiceProvider;
import com.rental_api.ServiceBooking.Entity.User;
import com.rental_api.ServiceBooking.Exception.ConflictException;
import com.rental_api.ServiceBooking.Exception.RequestNotFoundException;
import com.rental_api.ServiceBooking.Exception.UserNotFoundException;
import com.rental_api.ServiceBooking.Repository.ProviderRequestRepository;
import com.rental_api.ServiceBooking.Repository.RoleRepository;
import com.rental_api.ServiceBooking.Repository.ServiceProviderRepository;
import com.rental_api.ServiceBooking.Repository.UserRepository;
import com.rental_api.ServiceBooking.Services.ProviderRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProviderRequestServiceImpl implements ProviderRequestService {

    private final ProviderRequestRepository providerRequestRepository;
    private final ServiceProviderRepository serviceProviderRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    // CREATE REQUEST
    @Override
    @Transactional
    public ProviderRequestResponse createRequest(ProviderRequestDto dto, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        if (providerRequestRepository.existsByUserIdAndStatus(user.getId(), "PENDING")) {
            throw new ConflictException("You already have a pending provider request.");
        }

        Double experience = dto.getExperience() != null ? dto.getExperience() : 0.0;

        ProviderRequest request = ProviderRequest.builder()
                .user(user)
                .bio(dto.getBio())
                .experience(experience)
                .status("PENDING")
                .build();

        return mapToResponse(providerRequestRepository.save(request));
    }

    // GET ALL REQUESTS
    @Override
    @Transactional(readOnly = true)
    public List<ProviderRequestResponse> getAllRequests() {
        return providerRequestRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // APPROVE REQUEST
    @Override
    @Transactional
    public ProviderRequestResponse approveRequest(Long requestId) {
        ProviderRequest request = providerRequestRepository.findById(requestId)
                .orElseThrow(() -> new RequestNotFoundException(
                        "Provider request not found with id: " + requestId));

        if ("APPROVED".equals(request.getStatus())) {
            throw new ConflictException("Request already approved.");
        }

        if ("REJECTED".equals(request.getStatus())) {
            throw new ConflictException("Cannot approve a rejected request.");
        }

        // Approve request
        request.setStatus("APPROVED");
        providerRequestRepository.save(request);

        // Create ServiceProvider
        ServiceProvider provider = ServiceProvider.builder()
                .user(request.getUser())
                .bio(request.getBio())
                .experience(request.getExperience())
                .rating(0.0f)
                .build();
        serviceProviderRepository.save(provider);

        // Assign ROLE_PROVIDER
        User user = request.getUser();
        Role providerRole = roleRepository.findByName("PROVIDER")
                .orElseThrow(() -> new RuntimeException("ROLE_PROVIDER not found"));

        if (!user.getRoles().contains(providerRole)) {
            user.getRoles().add(providerRole);
            userRepository.save(user);
        }

        return mapToResponse(request);
    }

    // REJECT REQUEST
    @Override
    @Transactional
    public ProviderRequestResponse rejectRequest(Long requestId) {
        ProviderRequest request = providerRequestRepository.findById(requestId)
                .orElseThrow(() -> new RequestNotFoundException(
                        "Provider request not found with id: " + requestId));

        if ("REJECTED".equals(request.getStatus())) {
            throw new ConflictException("Request already rejected.");
        }

        if ("APPROVED".equals(request.getStatus())) {
            throw new ConflictException("Cannot reject an approved request.");
        }

        // Reject request
        request.setStatus("REJECTED");
        providerRequestRepository.save(request);

        return mapToResponse(request);
    }

    // MAP ENTITY TO RESPONSE
    private ProviderRequestResponse mapToResponse(ProviderRequest request) {
        User user = request.getUser();

        UserInfoResponse userInfo = UserInfoResponse.builder()
                .id(user.getId())
                .fullname(user.getFullname())
                .email(user.getEmail())
                .phone(user.getPhone())
                .location(user.getLocation())
                .build();

        return ProviderRequestResponse.builder()
                .id(request.getId())
                .user(userInfo)
                .bio(request.getBio())
                .experience(request.getExperience() != null ? String.valueOf(request.getExperience()) : "0.0")
                .status(request.getStatus())
                .build();
    }
}
