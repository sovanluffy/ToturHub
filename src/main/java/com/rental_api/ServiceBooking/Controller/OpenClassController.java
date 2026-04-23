package com.rental_api.ServiceBooking.Controller;

import com.rental_api.ServiceBooking.Dto.Request.OpenClassRequest;
import com.rental_api.ServiceBooking.Dto.Response.OpenClassResponse;
import com.rental_api.ServiceBooking.Dto.Response.TutorFullViewResponse;
import com.rental_api.ServiceBooking.Services.OpenClassService;
import com.rental_api.ServiceBooking.Services.TutorService;

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
    private final TutorService tutorService;

    // ================= CREATE =================
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

    // ================= PUBLIC CLASSES =================
    @Operation(summary = "Get all public open classes")
    @GetMapping("/public")
    public ResponseEntity<List<OpenClassResponse>> getAllPublicClasses() {
        return ResponseEntity.ok(
                openClassService.getAllPublicCards()
        );
    }

    // ================= TUTOR ALL CLASSES =================
    @Operation(summary = "Get all classes by tutor")
    @GetMapping("/tutor/{tutorId}")
    public ResponseEntity<List<OpenClassResponse>> getByTutor(@PathVariable Long tutorId) {
        return ResponseEntity.ok(
                openClassService.findByTutorId(tutorId)
        );
    }

    // ================= PUBLIC TUTOR CLASSES =================
    @Operation(summary = "Get public classes by tutor profile")
    @GetMapping("/tutor/{tutorId}/public")
    public ResponseEntity<List<OpenClassResponse>> getPublicByTutor(@PathVariable Long tutorId) {
        return ResponseEntity.ok(
                openClassService.getPublicClassesByTutor(tutorId)
        );
    }

    // ================= TUTOR PROFILE =================
    @Operation(summary = "Get tutor public profile")
    @GetMapping("/tutor/{tutorId}/profile")
    public ResponseEntity<TutorFullViewResponse> getTutorProfile(@PathVariable Long tutorId) {
        return ResponseEntity.ok(
                tutorService.getTutorFullDetail(tutorId)
        );
    }

    // ================= END CLASS =================
    @Operation(summary = "End class")
    @PatchMapping("/{id}/end")
    public ResponseEntity<OpenClassResponse> endClass(@PathVariable Long id) {
        return ResponseEntity.ok(
                openClassService.endClass(id)
        );
    }

    // ================= REOPEN CLASS =================
    @Operation(summary = "Reopen class")
    @PatchMapping("/{id}/reopen")
    public ResponseEntity<OpenClassResponse> reopenClass(@PathVariable Long id) {
        return ResponseEntity.ok(
                openClassService.reopenClass(id)
        );
    }

    // ================= COPY CLASS =================
    @Operation(summary = "Copy class")
    @PostMapping("/{id}/copy")
    public ResponseEntity<OpenClassResponse> copyClass(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(openClassService.copyClass(id));
    }

    // ================= DELETE =================
    @Operation(summary = "Delete class")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        openClassService.deleteClass(id);
        return ResponseEntity.noContent().build();
    }
}