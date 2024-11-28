package com.example.identityservice.client;

import com.example.identityservice.dto.ApiResponse;
import com.example.identityservice.dto.response.course.CourseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "course-service", url = "http://localhost:8002/course")
public interface CourseClient {
    @GetMapping("/{courseId}")
    ApiResponse<CourseResponse> getCourseById(@PathVariable("courseId") String courseId);
}
