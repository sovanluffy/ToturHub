package com.rental_api.ServiceBooking.Controller;

import com.rental_api.ServiceBooking.Dto.Request.OpenClassRequest;
import com.rental_api.ServiceBooking.Dto.Response.OpenClassResponse;
import com.rental_api.ServiceBooking.Services.OpenClassService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/open-classes")
@RequiredArgsConstructor
@Tag(name = "OpenClass Management", description = "Endpoints for tutors/admins to manage open classes")
public class OpenClassController {

    private final OpenClassService openClassService;

    // ------------------- CREATE / UPDATE -------------------

    @Operation(summary = "Create a new class")
    @PostMapping
    public ResponseEntity<OpenClassResponse> createClass(@RequestBody OpenClassRequest request) {
        OpenClassResponse response = openClassService.createClass(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing class")
    @PutMapping("/{id}")
    public ResponseEntity<OpenClassResponse> updateClass(
            @PathVariable Long id,
            @RequestBody OpenClassRequest request
    ) {
        return ResponseEntity.ok(openClassService.updateClass(id, request));
    }

    // ------------------- GET DETAILS -------------------

    @Operation(summary = "Get class details")
    @GetMapping("/{id}")
    public ResponseEntity<OpenClassResponse> getDetails(@PathVariable Long id) {
        return ResponseEntity.ok(openClassService.getClassDetails(id));
    }

    // ------------------- TUTOR'S CLASSES -------------------

    @Operation(summary = "Get all classes by tutor ID")
    @GetMapping("/tutor/{tutorId}")
    public ResponseEntity<List<OpenClassResponse>> getByTutor(@PathVariable Long tutorId) {
        return ResponseEntity.ok(openClassService.findByTutorId(tutorId));
    }

    // ✅ PUBLIC CARDS endpoint removed
}