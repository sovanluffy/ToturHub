package com.rental_api.ServiceBooking.Services.impl;

import com.rental_api.ServiceBooking.Services.CloudinaryService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Primary // This ensures Spring picks this instead of the Cloudinary version
public class FileSystemServiceImpl implements CloudinaryService {

    // The folder where files will be stored (relative to your project root)
    private final String UPLOAD_DIR = "uploads/";

    @Override
    public String uploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            // 1. Create the 'uploads' directory if it does not exist
            File directory = new File(UPLOAD_DIR);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // 2. Generate a unique filename: UUID_OriginalName.ext
            // Example: a1b2-c3d4_myphoto.jpg
            String originalFileName = file.getOriginalFilename();
            String cleanFileName = originalFileName != null ? originalFileName.replaceAll("\\s+", "_") : "file";
            String uniqueFileName = UUID.randomUUID().toString() + "_" + cleanFileName;

            // 3. Define the destination path
            Path path = Paths.get(UPLOAD_DIR + uniqueFileName);

            // 4. Save the file to the local disk
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

            // 5. Return the URL path that the frontend will use
            // This matches the ResourceHandler we will set up next
            return "/uploads/" + uniqueFileName;

        } catch (IOException e) {
            throw new RuntimeException("Failed to store file locally: " + e.getMessage());
        }
    }
}