package com.rental_api.ServiceBooking.Controller;

import com.rental_api.ServiceBooking.Dto.Request.OpenClassRequest;
import com.rental_api.ServiceBooking.Dto.Response.OpenClassResponse;
import com.rental_api.ServiceBooking.Services.OpenClassService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/open-classes")
@RequiredArgsConstructor
@Tag(name = "OpenClass Management")
public class OpenClassController {

    private final OpenClassService openClassService;

    // ================= CREATE (WITH IMAGE) =================
    @Operation(summary = "Create Open Class with image")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<OpenClassResponse> createClass(
            @RequestPart("data") OpenClassRequest request,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(openClassService.createClassWithImage(request, imageFile));
    }

    // ================= UPDATE =================
    @Operation(summary = "Update Open Class")
    @PutMapping("/{id}")
    public ResponseEntity<OpenClassResponse> updateClass(
            @PathVariable Long id,
            @RequestBody OpenClassRequest request
    ) {
        return ResponseEntity.ok(
                openClassService.updateClass(id, request)
        );
    }

    // ================= GET BY ID =================
    @Operation(summary = "Get class details")
    @GetMapping("/{id}")
    public ResponseEntity<OpenClassResponse> getDetails(@PathVariable Long id) {
        return ResponseEntity.ok(
                openClassService.getClassDetails(id)
        );
    }

    // ================= PUBLIC GET ALL (NO TOKEN) =================
    @Operation(summary = "Get all open classes (public, no auth required)")
    @GetMapping("/public")
    public ResponseEntity<List<OpenClassResponse>> getAllPublicClasses() {
        return ResponseEntity.ok(
                openClassService.getAllPublicCards()
        );
    }

    // ================= GET BY TUTOR =================
    @Operation(summary = "Get classes by tutor")
    @GetMapping("/tutor/{tutorId}")
    public ResponseEntity<List<OpenClassResponse>> getByTutor(@PathVariable Long tutorId) {
        return ResponseEntity.ok(
                openClassService.findByTutorId(tutorId)
        );
    }

    // ================= DELETE =================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        openClassService.deleteClass(id);
        return ResponseEntity.noContent().build();
    }
}