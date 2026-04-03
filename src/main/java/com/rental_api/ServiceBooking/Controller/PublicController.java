package com.rental_api.ServiceBooking.Controller;

import com.rental_api.ServiceBooking.Dto.Response.ApiResponse;
import com.rental_api.ServiceBooking.Dto.Response.OpenClassResponse;
import com.rental_api.ServiceBooking.Dto.Response.TutorCardResponse;
import com.rental_api.ServiceBooking.Services.OpenClassService;
import com.rental_api.ServiceBooking.Services.Public.PublicTutorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public")
@RequiredArgsConstructor
@Tag(name = "Public Discovery", description = "Endpoints for guests to explore tutors and classes")
public class PublicController {

    private final PublicTutorService publicTutorService;
    private final OpenClassService openClassService;

    /**
     * GET ALL TUTOR CARDS
     * Used for the main "Explore Tutors" section.
     */
    @GetMapping("/tutor-cards")
    @Operation(summary = "Get all public tutor cards", description = "Returns basic tutor info for homepage discovery grid")
    public ResponseEntity<ApiResponse<List<TutorCardResponse>>> getAllPublicTutors() {
        List<TutorCardResponse> tutors = publicTutorService.getAllPublicTutors();
        return ResponseEntity.ok(ApiResponse.success(tutors));
    }

    /**
     * VIEW CLASS DETAILS
     * Guests can click a card to see the full class details and available time slots.
     */
    @GetMapping("/classes/{id}")
    @Operation(summary = "Get class details", description = "Returns full details for a specific class including unbooked time slots")
    public ResponseEntity<ApiResponse<OpenClassResponse>> getClassDetail(@PathVariable Long id) {
        OpenClassResponse detail = openClassService.getClassDetails(id);
        return ResponseEntity.ok(ApiResponse.success(detail));
    }
}