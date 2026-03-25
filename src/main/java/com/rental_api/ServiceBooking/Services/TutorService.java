package com.rental_api.ServiceBooking.Services;

import com.rental_api.ServiceBooking.Dto.Request.TutorRequestDto;
import com.rental_api.ServiceBooking.Dto.Response.TutorResponseDto;
import java.util.List;

public interface TutorService {
    List<TutorResponseDto> getAllTutors();
    TutorResponseDto getTutorById(Long id);
    TutorResponseDto createTutor(TutorRequestDto request);
    TutorResponseDto updateTutor(Long id, TutorRequestDto request);
    void deleteTutor(Long id);
}