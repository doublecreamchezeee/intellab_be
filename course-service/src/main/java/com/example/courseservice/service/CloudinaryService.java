package com.example.courseservice.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class CloudinaryService {
    final Cloudinary cloudinary;

    public String uploadImage(MultipartFile file, String filename, String folderName) {
        try {
            Map<?, ?> options = ObjectUtils.asMap(
                    "public_id", filename,
                    "use_filename", true,
                    "unique_filename", true,
                    "overwrite", true,
                    "folder", folderName
            );

            Map<?, ?> uploadedFile = cloudinary.uploader().upload(file.getBytes(), options);
            String publicId = (String) uploadedFile.get("public_id");
            return cloudinary.url().secure(true).generate(publicId);
        } catch (IOException e) {
            log.error("Error uploading image: {}", e.getMessage());
            return null;
        }
    }

    public boolean deleteImage(String url) {
        //check xem có phải ảnh của cloudinary không
        if (!url.contains("cloudinary")) {
            System.out.println(url + " is not a cloudinary image");
            return false;
        }
        // Get ảnh từ url
        // ví dụ: https://res.cloudinary.com/diyn1vkim/image/upload/v1/CourseAvatar/95713603-63d1-4b75-8a89-1acdc0977459?_a=DAGAACARZAA0
        // publicId = CourseAvatar/95713603-63d1-4b75-8a89-1acdc0977459
        // publicId = url.substring(url.indexOf("CourseAvatar/"));
        String publicId = url.substring(url.indexOf("CourseAvatar/"));
        publicId = publicId.substring(0, publicId.indexOf("?"));
        System.out.println("publicId: "+publicId);
        try {
            Map result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());

            String resultStatus = (String) result.get("result");
            if (resultStatus.equals("ok")) {
                log.info("Image deleted successfully: {}", publicId);
                return true;
            } else {
                log.error("Failed to delete image: {}", publicId);
                return false;
            }
        } catch(IOException e) {
            log.error("Error deleting image: {}", e.getMessage());
            return false;
        }
    }
}
