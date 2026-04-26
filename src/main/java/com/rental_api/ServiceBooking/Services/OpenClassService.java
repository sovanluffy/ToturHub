package com.rental_api.ServiceBooking.Services;

import com.rental_api.ServiceBooking.Dto.Request.OpenClassRequest;
import com.rental_api.ServiceBooking.Dto.Response.OpenClassResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface OpenClassService {

    OpenClassResponse createClass(OpenClassRequest request);

    OpenClassResponse createClassWithImage(OpenClassRequest request, MultipartFile imageFile);

    OpenClassResponse updateClass(Long id, OpenClassRequest request);

    OpenClassResponse getClassDetails(Long id);

    List<OpenClassResponse> findByTutorId(Long tutorId);

    List<OpenClassResponse> getAllPublicCards();

    List<OpenClassResponse> getPublicClassesByTutor(Long tutorId);

    void deleteClass(Long id);

    List<OpenClassResponse> filterOpenClasses(String location, String subject);

    OpenClassResponse endClass(Long id);

    OpenClassResponse reopenClass(Long id);

    OpenClassResponse copyClass(Long id);

    // 🔥 NEW ENDPOINT METHOD
    List<OpenClassResponse.StudentPublicResponse> getStudentsByClass(Long classId);
}