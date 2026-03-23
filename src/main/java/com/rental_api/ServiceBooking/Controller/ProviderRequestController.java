package com.rental_api.ServiceBooking.Controller;

import com.rental_api.ServiceBooking.Dto.Request.ProviderRequestDto;
import com.rental_api.ServiceBooking.Dto.Response.ProviderRequestResponse;
import com.rental_api.ServiceBooking.Services.ProviderRequestService;
import com.rental_api.ServiceBooking.Security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/provider-requests")
@RequiredArgsConstructor
public class ProviderRequestController {

    private final ProviderRequestService providerRequestService;
    private final JwtUtils jwtUtils;

    @PostMapping("/request")
    public ProviderRequestResponse createRequest(@RequestBody ProviderRequestDto dto,
                                                 HttpServletRequest request) {
        String token = extractToken(request);
        String email = jwtUtils.extractEmail(token); // ✅ FIXED
        return providerRequestService.createRequest(dto, email);
    }

    @GetMapping("/all")
    public List<ProviderRequestResponse> getAllRequests() {
        return providerRequestService.getAllRequests();
    }

    @PutMapping("/{id}/approve")
    public ProviderRequestResponse approveRequest(@PathVariable Long id) {
        return providerRequestService.approveRequest(id);
    }

    @PutMapping("/{id}/reject")
    public ProviderRequestResponse rejectRequest(@PathVariable Long id) {
        return providerRequestService.rejectRequest(id);
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }
        return authHeader.substring(7);
    }
}
