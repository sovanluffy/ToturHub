package com.rental_api.ServiceBooking.Controller;

import com.rental_api.ServiceBooking.Dto.Request.OpenClassRequest;
import com.rental_api.ServiceBooking.Dto.Response.OpenClassResponse;
import com.rental_api.ServiceBooking.Dto.Response.TutorCardResponse;
import com.rental_api.ServiceBooking.Services.impl.OpenClassServiceImpl; // Cast to impl for image support
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/open-classes")
@RequiredArgsConstructor
public class OpenClassController {

    private final OpenClassServiceImpl openClassService; // Using Impl to access createClassWithImage

    /**
     * ✅ CREATE: Supports Multi-Schedule JSON + Image File
     * Uses: multipart/form-data
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<OpenClassResponse> createClass(
            @RequestPart("data") OpenClassRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        // This calls the logic that handles Daily/Weekend generation
        OpenClassResponse response = openClassService.createClassWithImage(request, image);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * ✅ UPDATE: Update text data and refresh schedules
     */
    @PutMapping("/{id}")
    public ResponseEntity<OpenClassResponse> updateClass(
            @PathVariable Long id,
            @RequestBody OpenClassRequest request
    ) {
        return ResponseEntity.ok(openClassService.updateClass(id, request));
    }

    /**
     * ✅ SEARCH: Advanced filtering (City, District, Subject, Price, Experience)
     */
    @GetMapping("/search")
    public ResponseEntity<List<OpenClassResponse>> search(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Integer minExp
    ) {
        return ResponseEntity.ok(openClassService.searchClasses(city, district, subjectId, maxPrice, minExp));
    }

    /**
     * ✅ GET DETAILS: View full class details including generated slots
     */
    @GetMapping("/{id}")
    public ResponseEntity<OpenClassResponse> getDetails(@PathVariable Long id) {
        return ResponseEntity.ok(openClassService.getClassDetails(id));
    }

    /**
     * ✅ PUBLIC CARDS: For browsing tutors on the home page
     */
    @GetMapping("/public-cards")
    public ResponseEntity<List<TutorCardResponse>> getPublicCards() {
        return ResponseEntity.ok(openClassService.getAllPublicCards());
    }

    /**
     * ✅ TUTOR'S CLASSES: View all listings by a specific tutor
     */
    @GetMapping("/tutor/{tutorId}")
    public ResponseEntity<List<OpenClassResponse>> getByTutor(@PathVariable Long tutorId) {
        return ResponseEntity.ok(openClassService.findByTutorId(tutorId));
    }

    /**
     * ✅ DELETE: Remove a listing and all its schedules
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        openClassService.deleteClass(id);
        return ResponseEntity.noContent().build();
    }
}