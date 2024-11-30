package com.example.courseservice.controller;

import com.example.courseservice.dto.ApiResponse;
import com.example.courseservice.dto.request.course.CourseCreationRequest;
import com.example.courseservice.dto.request.course.CourseUpdateRequest;
import com.example.courseservice.dto.request.course.EnrollCourseRequest;
import com.example.courseservice.dto.response.course.CourseResponse;
import com.example.courseservice.dto.response.course.DetailCourseResponse;
import com.example.courseservice.dto.response.lesson.LessonResponse;
import com.example.courseservice.model.EnrollCourse;
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
@RequestMapping("/courses")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CourseController {
    CourseService courseService;
    LessonService lessonService;

    @PostMapping("")
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
    ApiResponse<DetailCourseResponse> getCourseById(@PathVariable("courseId") String courseId, @RequestParam(required = false) String userUid) {
        return ApiResponse.<DetailCourseResponse>builder()
                .result(courseService.getCourseById(courseId, userUid))
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
    public ApiResponse<List<CourseResponse>> searchCourses(@RequestParam("keyword") String keyword) {
        return ApiResponse.<List<CourseResponse>>builder()
                .result(courseService.searchCourses(keyword))
                .build();
    }

    @PostMapping("/enroll")
    public ApiResponse<EnrollCourse> enrollCourse(@RequestBody @Valid EnrollCourseRequest request) {

        return ApiResponse.<EnrollCourse>builder()
                .result(courseService.enrollCourse(request.getUserUid(), request.getCourseId()))
                .build();
    }

    @GetMapping("/enrollCourses/{userUid}/")
    public ApiResponse<List<CourseResponse>> getUserCourses(@PathVariable("userUid") String userUid) {
        return ApiResponse.<List<CourseResponse>>builder()
                .result(courseService.getUserCourses(userUid))
                .build();
    }
}
