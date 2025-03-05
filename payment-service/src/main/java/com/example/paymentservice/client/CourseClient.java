package com.example.paymentservice.client;


import com.example.paymentservice.dto.ApiResponse;
import com.example.paymentservice.dto.response.course.DetailCourseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@Component
@FeignClient(name = "course-service", url = "${COURSE_SERVICE_URL}")
public interface CourseClient {
    @GetMapping(value = "/courses/{courseId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<DetailCourseResponse> getDetailCourseById(@PathVariable UUID courseId);

}
