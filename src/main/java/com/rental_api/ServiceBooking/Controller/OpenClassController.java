package com.rental_api.ServiceBooking.Controller;

import com.rental_api.ServiceBooking.Dto.Request.OpenClassRequest;
import com.rental_api.ServiceBooking.Dto.Response.OpenClassResponse;
import com.rental_api.ServiceBooking.Dto.Response.ApiResponse;
import com.rental_api.ServiceBooking.Services.OpenClassService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/classes")
@RequiredArgsConstructor
@Tag(name = "Open Class Management", description = "Endpoints for posting and searching tutor classes")
public class OpenClassController {

    private final OpenClassService openClassService;

    /**
     * ✅ POST A CLASS
     * Access: Authenticated Tutors
     */
    @PostMapping("/post")
    @Operation(summary = "Create a new class listing")
    public ResponseEntity<ApiResponse<OpenClassResponse>> createClass(@RequestBody OpenClassRequest request) {
        OpenClassResponse response = openClassService.createClass(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * ✅ ADVANCED SEARCH (PUBLIC)
     * Students use this to find classes with multiple filters.
     * Example: /api/v1/classes/search?city=Phnom Penh&maxPrice=20&minExp=2
     */
    @GetMapping("/search")
    @Operation(summary = "Advanced Search", description = "Filter by city, subject, price, and tutor experience")
    public ResponseEntity<ApiResponse<List<OpenClassResponse>>> searchClasses(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Integer minExp
    ) {
        List<OpenClassResponse> classes = openClassService.searchClasses(city, subjectId, maxPrice, minExp);
        return ResponseEntity.ok(ApiResponse.success(classes));
    }

    /**
     * ✅ VIEW SINGLE CLASS DETAIL
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get full details of a specific class listing")
    public ResponseEntity<ApiResponse<OpenClassResponse>> getClassDetails(@PathVariable Long id) {
        OpenClassResponse details = openClassService.getClassDetails(id);
        return ResponseEntity.ok(ApiResponse.success(details));
    }

    /**
     * ✅ GET CLASSES BY TUTOR
     * Useful for showing all classes on a specific Tutor's profile.
     */
    @GetMapping("/tutor/{tutorId}")
    @Operation(summary = "Get all classes offered by a specific tutor")
    public ResponseEntity<ApiResponse<List<OpenClassResponse>>> getByTutor(@PathVariable Long tutorId) {
        List<OpenClassResponse> classes = openClassService.findByTutorId(tutorId);
        return ResponseEntity.ok(ApiResponse.success(classes));
    }
}