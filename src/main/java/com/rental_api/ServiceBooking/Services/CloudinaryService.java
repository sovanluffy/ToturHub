package com.rental_api.ServiceBooking.Services;

import org.springframework.web.multipart.MultipartFile;

public interface CloudinaryService {
    String uploadFile(MultipartFile file);
    void deleteFile(String url);
}