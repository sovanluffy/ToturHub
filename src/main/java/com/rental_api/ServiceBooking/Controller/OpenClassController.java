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
            @RequestPart(value = "image", required = false) MultipartFile image) {
        return new ResponseEntity<>(
                openClassService.create(request, image),
                HttpStatus.CREATED);
    }

    // ================= UPDATE (WITH IMAGE) =================
    @Operation(summary = "Update Open Class with image")
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<OpenClassResponse> updateClass(
            @PathVariable Long id,
            @RequestPart("data") OpenClassRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        return ResponseEntity.ok(
                openClassService.update(id, request, image));
    }

    // ================= GET BY ID =================
    @Operation(summary = "Get class details")
    @GetMapping("/{id}")
    public ResponseEntity<OpenClassResponse> getDetails(@PathVariable Long id) {
        return ResponseEntity.ok(openClassService.getById(id));
    }

    // ================= GET BY TUTOR =================
    @Operation(summary = "Get classes by tutor")
    @GetMapping("/tutor/{tutorId}")
    public ResponseEntity<List<OpenClassResponse>> getByTutor(@PathVariable Long tutorId) {
        return ResponseEntity.ok(openClassService.getByTutor(tutorId));
    }
}