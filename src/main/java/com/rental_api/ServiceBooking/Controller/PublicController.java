package com.rental_api.ServiceBooking.Controller;

import com.rental_api.ServiceBooking.Dto.Response.TutorCardResponse;
import com.rental_api.ServiceBooking.Services.OpenClassService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public")
@RequiredArgsConstructor
public class PublicController {

    private final OpenClassService openClassService;

    @GetMapping("/tutor-cards")
    public ResponseEntity<List<TutorCardResponse>> getAllTutorCards() {
        List<TutorCardResponse> cards = openClassService.getAllPublicCards();
        return ResponseEntity.ok(cards);
    }
}