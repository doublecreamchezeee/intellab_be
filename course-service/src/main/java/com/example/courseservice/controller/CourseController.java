package com.example.courseservice.controller;

import com.example.courseservice.dto.ApiResponse;
import com.example.courseservice.dto.request.course.CourseCreationRequest;
import com.example.courseservice.dto.request.course.CourseUpdateRequest;
import com.example.courseservice.dto.request.course.EnrollCourseRequest;
import com.example.courseservice.dto.response.course.CourseCreationResponse;
import com.example.courseservice.dto.response.course.DetailCourseResponse;
import com.example.courseservice.dto.response.learningLesson.LessonProgressResponse;
import com.example.courseservice.dto.response.learningLesson.LessonUserResponse;
import com.example.courseservice.dto.response.lesson.LessonResponse;
import com.example.courseservice.model.UserCourses;
import com.example.courseservice.model.compositeKey.EnrollCourse;
import com.example.courseservice.service.CourseService;
import com.example.courseservice.service.LessonService;
import com.example.courseservice.utils.ParseUUID;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.rmi.server.UID;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CourseController {
    CourseService courseService;
    LessonService lessonService;

    @PostMapping("")
    ApiResponse<CourseCreationResponse> createCourse(@RequestBody @Valid CourseCreationRequest request) {
        return ApiResponse.<CourseCreationResponse>builder()
                .result(courseService.createCourse(
                        ParseUUID.normalizeUID(request.getUserUid()),
                        request))
                .build();
    }

    @GetMapping("/{courseId}/lessons")
    ApiResponse<List<LessonResponse>> getLessonsByCourseId(@PathVariable("courseId") String courseId) {
        return ApiResponse.<List<LessonResponse>>builder()
                .result(lessonService.getLessonsByCourseId(courseId))
                .build();
    }

    @GetMapping("/{courseId}/{userUid}/lessons")
    ApiResponse<List<LessonProgressResponse>> getLessonProgressByCourseIdAndUserUid(@PathVariable("courseId") String courseId, @PathVariable("userUid") String userUid) {
        return ApiResponse.<List<LessonProgressResponse>>builder()
                .result(lessonService.getLessonProgress(
                            ParseUUID.normalizeUID(userUid),
                            UUID.fromString(courseId)
                        )
                )
                .build();
    }

    @GetMapping("/{courseId}")
    ApiResponse<DetailCourseResponse> getCourseById(@PathVariable("courseId") UUID courseId, @RequestParam(required = false) String userUid) {
        UUID userUUID = null;
        if (userUid != null) {
            userUUID = ParseUUID.normalizeUID(userUid);
        }
        return ApiResponse.<DetailCourseResponse>builder()
                .result(courseService.getCourseById(courseId, userUUID))
                .build();
    }

    @GetMapping
    ApiResponse<List<CourseCreationResponse>> getAllCourse() {
        return ApiResponse.<List<CourseCreationResponse>>builder()
                .result(courseService.getAllCourses())
                .build();
    }

    @DeleteMapping("/{courseId}")
    ApiResponse<String> deleteCourseById(@PathVariable("courseId") UUID courseId) {
        courseService.deleteCourseById(courseId);
        return ApiResponse.<String>builder()
                .result("Course has been deleted")
                .build();
    }

    @PutMapping("/{courseId}")
    ApiResponse<CourseCreationResponse> updateCourse(@PathVariable("courseId") UUID courseId, @RequestBody CourseUpdateRequest request) {
        return ApiResponse.<CourseCreationResponse>builder()
                .result(courseService.updateCourse(courseId, request))
                .build();
    }

    @GetMapping("/search")
    public ApiResponse<List<CourseCreationResponse>> searchCourses(@RequestParam("keyword") String keyword) {
        return ApiResponse.<List<CourseCreationResponse>>builder()
                .result(courseService.searchCourses(keyword))
                .build();
    }

    @PostMapping("/enroll")
    public ApiResponse<UserCourses> enrollCourse(@RequestBody @Valid EnrollCourseRequest request) {

        return ApiResponse.<UserCourses>builder()
                .result(courseService.enrollCourse(ParseUUID.normalizeUID(request.getUserUid()), request.getCourseId()))
                .build();
    }

    @GetMapping("/{courseId}/users")
    public ApiResponse<List<UserCourses>> getEnrolledUsersOfCourse(@PathVariable("courseId") UUID courseId) {
        return ApiResponse.<List<UserCourses>>builder()
                .result(courseService.getEnrolledUsersOfCourse(courseId))
                .build();
    }

    @GetMapping("/{userUid}/enrolledCourses")
    public ApiResponse<List<UserCourses>> getEnrolledCoursesOfUser(@PathVariable("userUid") String userUid) {
        return ApiResponse.<List<UserCourses>>builder()
                .result(courseService.getEnrolledCoursesOfUser(ParseUUID.normalizeUID(userUid)))
                .build();
    }

}
