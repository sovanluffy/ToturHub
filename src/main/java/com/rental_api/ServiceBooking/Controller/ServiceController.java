package com.rental_api.ServiceBooking.Controller;

import com.rental_api.ServiceBooking.Dto.Request.ServiceRequest;
import com.rental_api.ServiceBooking.Dto.Response.ApiResponse;
import com.rental_api.ServiceBooking.Dto.Response.ServiceResponse;
import com.rental_api.ServiceBooking.Services.ServiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
@Tag(name = "Service", description = "Service management APIs")
public class ServiceController {

    private final ServiceService serviceService;

    @PostMapping
    @Operation(summary = "Create a new service")
    public ResponseEntity<ApiResponse<ServiceResponse>> create(
            @RequestBody ServiceRequest request,
            @RequestHeader("Authorization") String authHeader
    ) {
        String token = authHeader.replace("Bearer ", "");
        ServiceResponse response = serviceService.createService(request, token);
        return ResponseEntity.ok(ApiResponse.success(response, "Service created successfully"));
    }

    @GetMapping
    @Operation(summary = "Get all services")
    public ResponseEntity<ApiResponse<List<ServiceResponse>>> getAll() {
        List<ServiceResponse> list = serviceService.getAllServices();
        return ResponseEntity.ok(ApiResponse.success(list, "All services retrieved"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get service by ID")
    public ResponseEntity<ApiResponse<ServiceResponse>> getById(@PathVariable Long id) {
        ServiceResponse response = serviceService.getServiceById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Service retrieved"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a service")
    public ResponseEntity<ApiResponse<ServiceResponse>> update(
            @PathVariable Long id,
            @RequestBody ServiceRequest request
    ) {
        ServiceResponse response = serviceService.updateService(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Service updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a service")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        serviceService.deleteService(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Service deleted successfully"));
    }
}
