package com.rental_api.ServiceBooking.Controller;

import com.rental_api.ServiceBooking.Dto.Request.OpenClassRequest;
import com.rental_api.ServiceBooking.Dto.Response.OpenClassResponse;
import com.rental_api.ServiceBooking.Services.OpenClassService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/open-classes")
@RequiredArgsConstructor
public class OpenClassController {

    private final OpenClassService openClassService;

    /**
     * POST A CLASS: 
     * Tutors use this to create a new class listing. 
     * They can call this many times to post different subjects.
     */
    @PostMapping("/post")
    public ResponseEntity<OpenClassResponse> createClass(@RequestBody OpenClassRequest request) {
        OpenClassResponse response = openClassService.createClass(request);
        return ResponseEntity.ok(response);
    }

    /**
     * SEARCH CLASSES: 
     * Students use this to see all active classes.
     * Optional: Filter by city (e.g., ?city=Phnom Penh)
     */
    @GetMapping("/all")
    public ResponseEntity<List<OpenClassResponse>> getAllActiveClasses(
            @RequestParam(required = false) String city) {
        List<OpenClassResponse> classes = openClassService.findAllActiveClasses(city);
        return ResponseEntity.ok(classes);
    }

    /**
     * VIEW SINGLE CLASS DETAILS:
     * When a student clicks a specific post to see 
     * the Tutor's ratings, full bio, and available time slots.
     */
    @GetMapping("/{id}")
    public ResponseEntity<OpenClassResponse> getClassDetails(@PathVariable Long id) {
        OpenClassResponse details = openClassService.getClassDetails(id);
        return ResponseEntity.ok(details);
    }
}