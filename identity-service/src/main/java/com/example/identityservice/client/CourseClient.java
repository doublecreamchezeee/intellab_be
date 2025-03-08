package com.example.identityservice.client;


import com.example.identityservice.dto.ApiResponse;
import com.example.identityservice.dto.request.course.DisenrollCourseRequest;
import com.example.identityservice.dto.request.course.EnrollCourseRequest;
import com.example.identityservice.dto.response.course.DetailCourseResponse;
import com.example.identityservice.dto.response.userCourse.UserCoursesResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Component
@FeignClient(name = "course-service", url = "${COURSE_SERVICE_URL}")
public interface CourseClient {
    @GetMapping(value = "/courses/{courseId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<DetailCourseResponse> getDetailCourseById(@PathVariable UUID courseId);

    @PostMapping(value = "/courses/enrollPaidCourse", consumes = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<UserCoursesResponse> enrollPaidCourse(@RequestBody EnrollCourseRequest request);

    @PostMapping(value = "/courses/disenroll", consumes = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<Boolean> disenrollCourse(@RequestBody DisenrollCourseRequest request);

}
