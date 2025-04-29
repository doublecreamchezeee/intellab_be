package com.example.courseservice.configuration;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {
    @Value("${cloudinary.url}")
    private String cloudinaryUrl;

    @Bean
    Cloudinary getCloudinary() {
        return new Cloudinary(cloudinaryUrl);
    }
}
