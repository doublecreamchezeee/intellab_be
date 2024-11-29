package com.example.courseservice.controller;

import com.example.courseservice.dto.ApiResponse;
import com.example.courseservice.dto.request.CourseCreationRequest;
import com.example.courseservice.dto.request.CourseUpdateRequest;
import com.example.courseservice.dto.response.CourseResponse;
import com.example.courseservice.dto.response.LessonResponse;
import com.example.courseservice.service.CourseService;
import com.example.courseservice.service.LessonService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CourseController {
    CourseService courseService;
    LessonService lessonService;

    @PostMapping
    ApiResponse<CourseResponse> createCourse(@RequestBody @Valid CourseCreationRequest request) {
        return ApiResponse.<CourseResponse>builder()
                .result(courseService.createCourse(request))
                .build();
    }

    @GetMapping("/{courseId}/lessons")
    ApiResponse<List<LessonResponse>> getLessonsByCourseId(@PathVariable("courseId") String courseId) {
        return ApiResponse.<List<LessonResponse>>builder()
                .result(lessonService.getLessonsByCourseId(courseId))
                .build();
    }

    @GetMapping("/{courseId}")
    ApiResponse<CourseResponse> getCourseById(@PathVariable("courseId") String courseId) {
        return ApiResponse.<CourseResponse>builder()
                .result(courseService.getCourseById(courseId))
                .build();
    }


    @GetMapping
    ApiResponse<List<CourseResponse>> getAllCourse() {
        return ApiResponse.<List<CourseResponse>>builder()
                .result(courseService.getAllCourses())
                .build();
    }

    @DeleteMapping("/{courseId}")
    ApiResponse<String> deleteCourseById(@PathVariable("courseId") String courseId) {
        courseService.deleteCourseById(courseId);
        return ApiResponse.<String>builder()
                .result("Course has been deleted")
                .build();
    }

    @PutMapping("/{courseId}")
    ApiResponse<CourseResponse> updateCourse(@PathVariable("courseId") String courseId, @RequestBody CourseUpdateRequest request) {
        return ApiResponse.<CourseResponse>builder()
                .result(courseService.updateCourse(courseId, request))
                .build();
    }

    @GetMapping("/search")
    public ApiResponse<List<CourseResponse>> searchCourses(@RequestParam String keyword) {
        return ApiResponse.<List<CourseResponse>>builder()
                .result(courseService.searchCourses(keyword))
                .build();
    }
}
