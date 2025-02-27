package com.example.problemservice.configuration;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /*@Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }*/

    @Value("${mount_path}")
    public String mountPath;

    @Bean
    public String getMountPath() {
        return mountPath;
    }
}