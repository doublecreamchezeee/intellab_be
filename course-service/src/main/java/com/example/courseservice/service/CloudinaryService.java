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
                    "unique_filename", false,
                    "overwrite", true,
                    "folder", folderName
            );

            Map<?, ?> uploadedFile = cloudinary.uploader().upload(file.getBytes(), options);
            String publicId = (String) uploadedFile.get("public_id");
            return cloudinary.url().secure(true).generate(publicId);
        } catch (IOException e) {
            e.printStackTrace();;
            return null;
        }
    }

    public boolean deleteImage(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            return true;
        } catch(IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
