package com.rental_api.ServiceBooking.Controller;

import com.rental_api.ServiceBooking.Dto.Request.TutorRequestDto;
import com.rental_api.ServiceBooking.Dto.Response.TutorResponseDto;
import com.rental_api.ServiceBooking.Services.TutorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tutors")
@RequiredArgsConstructor
@Tag(name = "Tutor Management", description = "Public and Private endpoints for Tutor profiles")
public class TutorController {

    private final TutorService tutorService;

    // -------------------------------------------------------------------------
    // PUBLIC ENDPOINTS
    // -------------------------------------------------------------------------

    @Operation(summary = "Get all tutors", description = "Returns a list of all active tutors for the marketplace.")
    @GetMapping
    public ResponseEntity<List<TutorResponseDto>> getAllTutors() {
        return ResponseEntity.ok(tutorService.getAllTutors());
    }

    @Operation(summary = "Get tutor by ID", description = "Fetch detailed profile of a single tutor.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Found the tutor"),
        @ApiResponse(responseCode = "404", description = "Tutor not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<TutorResponseDto> getTutorById(@PathVariable Long id) {
        return ResponseEntity.ok(tutorService.getTutorById(id));
    }

    // -------------------------------------------------------------------------
    // PRIVATE ENDPOINTS (Role Restricted)
    // -------------------------------------------------------------------------

    @Operation(
        summary = "Create tutor profile details", 
        description = "Accessible only by users with 'tutor' role. links professional info to the user.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("hasRole('tutor')")
    @PostMapping
    public ResponseEntity<TutorResponseDto> createTutorProfile(@RequestBody TutorRequestDto requestDto) {
        TutorResponseDto response = tutorService.createTutor(requestDto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(
        summary = "Update tutor profile", 
        description = "Allows a tutor to update their bio, price, or subjects.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("hasRole('tutor')")
    @PutMapping("/{id}")
    public ResponseEntity<TutorResponseDto> updateTutorProfile(
            @PathVariable Long id, 
            @RequestBody TutorRequestDto requestDto) {
        TutorResponseDto response = tutorService.updateTutor(id, requestDto);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Delete tutor profile", 
        description = "Removes the tutor profile from the system.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("hasRole('tutor') or hasRole('admin')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTutor(@PathVariable Long id) {
        tutorService.deleteTutor(id);
        return ResponseEntity.noContent().build();
    }
}