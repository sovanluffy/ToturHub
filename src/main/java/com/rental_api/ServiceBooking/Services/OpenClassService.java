package com.rental_api.ServiceBooking.Services;

import com.rental_api.ServiceBooking.Dto.Request.OpenClassRequest;
import com.rental_api.ServiceBooking.Dto.Response.OpenClassResponse;
import com.rental_api.ServiceBooking.Dto.Response.TutorCardResponse;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

public interface OpenClassService {

    OpenClassResponse createClassWithImage(OpenClassRequest request, MultipartFile imageFile);

    OpenClassResponse createClass(OpenClassRequest request);

    OpenClassResponse updateClass(Long id, OpenClassRequest request);

    OpenClassResponse getClassDetails(Long id);

    List<TutorCardResponse> getAllPublicCards();

    List<OpenClassResponse> findByTutorId(Long tutorId);

    void deleteClass(Long id);
}