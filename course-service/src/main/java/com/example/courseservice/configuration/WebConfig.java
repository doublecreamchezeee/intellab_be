package com.example.courseservice.configuration;


import com.example.courseservice.interceptor.UserHeaderInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final UserHeaderInterceptor userHeaderInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        System.out.println("Adding UserHeaderInterceptor to the registry");
        registry.addInterceptor(userHeaderInterceptor)
                .addPathPatterns("/**"); // Apply to all paths
    }
}
