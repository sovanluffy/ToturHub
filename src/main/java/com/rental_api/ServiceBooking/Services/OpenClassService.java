package com.rental_api.ServiceBooking.Services;

import com.rental_api.ServiceBooking.Dto.Request.OpenClassRequest;
import com.rental_api.ServiceBooking.Dto.Response.OpenClassResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface OpenClassService {

    // =========================================================
    // 🟢 CREATE CLASS
    // =========================================================
    OpenClassResponse createClass(OpenClassRequest request);

    OpenClassResponse createClassWithImage(
            OpenClassRequest request,
            MultipartFile imageFile
    );

    // =========================================================
    // ✏️ UPDATE CLASS
    // =========================================================
    OpenClassResponse updateClass(
            Long id,
            OpenClassRequest request
    );

    // =========================================================
    // 🔍 GET CLASS DETAILS
    // =========================================================
    OpenClassResponse getClassDetails(Long id);

    // =========================================================
    // 👤 OWNER VIEW (ALL CLASSES)
    // =========================================================
    List<OpenClassResponse> findByTutorId(Long tutorId);

    // =========================================================
    // 🌍 PUBLIC CLASSES (GLOBAL FEED)
    // =========================================================
    List<OpenClassResponse> getAllPublicCards();

    // =========================================================
    // 👀 PUBLIC TUTOR PROFILE CLASSES
    // =========================================================
    List<OpenClassResponse> getPublicClassesByTutor(Long tutorId);

    // =========================================================
    // 🗑️ DELETE CLASS
    // =========================================================
    void deleteClass(Long id);

    List<OpenClassResponse> filterOpenClasses(String location, String subject);

    // =========================================================
    // 🔴 CLASS STATUS CONTROL (NEW FEATURE)
    // =========================================================

    OpenClassResponse endClass(Long id);

    OpenClassResponse reopenClass(Long id);

    OpenClassResponse copyClass(Long id);
}