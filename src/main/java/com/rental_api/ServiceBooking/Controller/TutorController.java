package com.rental_api.ServiceBooking.Controller;

import com.rental_api.ServiceBooking.Dto.Request.TutorProfileRequest;
import com.rental_api.ServiceBooking.Dto.Response.ApiResponse;
import com.rental_api.ServiceBooking.Dto.Response.TutorFullViewResponse;
import com.rental_api.ServiceBooking.Services.TutorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tutors")
@RequiredArgsConstructor
@Tag(name = "Tutor Management", description = "Endpoints for professional tutor profiles and media")
public class TutorController {

    private final TutorService tutorService;

    /**
     * UPDATE TUTOR PROFILE
     * This endpoint consumes multipart/form-data.
     * 'data' part contains the JSON (Bio, Education, Experience).
     * Other parts contain the actual binary files.
     */
    @PutMapping(
        value = "/profile", 
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
        summary = "Update Tutor Professional Profile",
        description = "Saves professional details (JSON) and uploads files to local storage."
    )
    public ResponseEntity<ApiResponse<String>> updateProfile(
            @Valid @RequestPart("data") 
            @Parameter(schema = @Schema(implementation = TutorProfileRequest.class)) 
            TutorProfileRequest request,

            @RequestPart(value = "profileImg", required = false) 
            MultipartFile profileImg,

            @RequestPart(value = "videoFile", required = false) 
            MultipartFile videoFile,

            @RequestPart(value = "certs", required = false) 
            List<MultipartFile> certs
    ) {
        // Pass all parts to the service layer
        tutorService.updateTutorProfile(request, profileImg, videoFile, certs);
        
        return ResponseEntity.ok(ApiResponse.success("Tutor profile and media updated successfully!"));
    }

    /**
     * GET TUTOR FULL DETAIL
     * Returns the complete profile view for the frontend.
     */
    @GetMapping("/{id}/full-view")
    @Operation(
        summary = "Get Full Tutor Details", 
        description = "Returns bio, local file paths, education, and experience for a specific tutor ID."
    )
    public ResponseEntity<ApiResponse<TutorFullViewResponse>> getFullTutorDetail(@PathVariable Long id) {
        TutorFullViewResponse response = tutorService.getTutorFullDetail(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}