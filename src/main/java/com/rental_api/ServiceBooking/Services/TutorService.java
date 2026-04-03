package com.rental_api.ServiceBooking.Services;

import com.rental_api.ServiceBooking.Dto.Request.TutorProfileRequest;
import com.rental_api.ServiceBooking.Dto.Response.TutorFullViewResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface TutorService {

    void updateTutorProfile(TutorProfileRequest request,
                            MultipartFile profileImg,
                            MultipartFile videoFile,
                            MultipartFile coverImage,
                            List<MultipartFile> certificates);

    void publishProfile();

    void unpublishProfile();
    void adminUnpublishTutor(Long tutorId);

    TutorFullViewResponse getMyOwnProfile();

    TutorFullViewResponse getTutorFullDetail(Long tutorId);

    void incrementStudentCount(Long tutorId);
}