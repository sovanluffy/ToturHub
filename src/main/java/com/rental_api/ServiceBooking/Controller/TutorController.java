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
@Tag(name = "Tutor Management", description = "Endpoints for private profile management and publishing")
public class TutorController {

    private final TutorService tutorService;

    /**
     * UPDATE PROFILE (Saves as Draft)
     */
    @PutMapping(
        value = "/profile", 
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(summary = "Update Profile", description = "Saves info but keeps it private (isPublic=false).")
    public ResponseEntity<ApiResponse<String>> updateProfile(
            @Valid @RequestPart("data") 
            @Parameter(schema = @Schema(implementation = TutorProfileRequest.class)) 
            TutorProfileRequest request,

            @RequestPart(value = "profileImg", required = false) MultipartFile profileImg,
            @RequestPart(value = "videoFile", required = false) MultipartFile videoFile,
            @RequestPart(value = "certs", required = false) List<MultipartFile> certs
    ) {
        tutorService.updateTutorProfile(request, profileImg, videoFile, certs);
        return ResponseEntity.ok(ApiResponse.success("Profile saved successfully as draft!"));
    }

    /**
     * ✅ NEW: PUBLISH PROFILE (The "Post" Button)
     */
    @PostMapping("/publish")
    @Operation(summary = "Post to Public", description = "Set isPublic to true.")
    public ResponseEntity<ApiResponse<String>> publish() {
        tutorService.publishProfile();
        return ResponseEntity.ok(ApiResponse.success("Your profile is now live!"));
    }

    @PostMapping("/unpublish")
    @Operation(summary = "Hide Profile", description = "Set isPublic to false. You will vanish from the public list.")
    public ResponseEntity<ApiResponse<String>> unpublish() {
        tutorService.unpublishProfile();
        return ResponseEntity.ok(ApiResponse.success("Your profile is now hidden."));
    }

    /**
     * VIEW OWN FULL DETAIL
     */
    @GetMapping("/{id}/full-view")
    @Operation(summary = "Get Full View", description = "Returns full details including the isPublic status.")
    public ResponseEntity<ApiResponse<TutorFullViewResponse>> getFullTutorDetail(@PathVariable Long id) {
        TutorFullViewResponse response = tutorService.getTutorFullDetail(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}