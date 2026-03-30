package com.rental_api.ServiceBooking.Services.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.rental_api.ServiceBooking.Services.CloudinaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Service
@Primary 
@RequiredArgsConstructor
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;

    @Override
    public String uploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            // Uploads the file bytes to Cloudinary
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", "user_avatars",
                    "resource_type", "auto"
            ));

            // Returns the secure URL (https://res.cloudinary.com/...)
            return uploadResult.get("secure_url").toString();

        } catch (IOException e) {
            log.error("Cloudinary upload failed", e);
            throw new RuntimeException("Failed to upload image to Cloudinary");
        }
    }

    @Override
    public void deleteFile(String url) {
        if (url == null || url.isEmpty()) return;

        try {
            // Extracts the ID from the URL to delete from Cloud
            String publicId = extractPublicId(url);
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("Deleted image from Cloudinary: {}", publicId);
        } catch (IOException e) {
            log.error("Failed to delete image from Cloudinary", e);
        }
    }

    private String extractPublicId(String url) {
        // Gets the ID between the last '/' and the last '.'
        int lastSlash = url.lastIndexOf("/") + 1;
        int lastDot = url.lastIndexOf(".");
        return "user_avatars/" + url.substring(lastSlash, lastDot);
    }
}