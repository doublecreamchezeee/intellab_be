package com.example.courseservice.controller;

import com.example.courseservice.dto.ApiResponse;
import com.example.courseservice.dto.request.course.CourseCreationRequest;
import com.example.courseservice.dto.request.course.CourseUpdateRequest;
import com.example.courseservice.dto.request.course.EnrollCourseRequest;
import com.example.courseservice.dto.response.course.CourseCreationResponse;
import com.example.courseservice.dto.response.course.DetailCourseResponse;
import com.example.courseservice.dto.response.learningLesson.LessonProgressResponse;
import com.example.courseservice.dto.response.lesson.LessonResponse;
import com.example.courseservice.dto.response.userCourses.EnrolledCourseResponse;
import com.example.courseservice.model.UserCourses;
import com.example.courseservice.service.CourseService;
import com.example.courseservice.service.LessonService;
import com.example.courseservice.utils.ParseUUID;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "Course")
public class CourseController {
    CourseService courseService;
    LessonService lessonService;

    @Operation(
            summary = "Create course"
    )
    @PostMapping("")
    ApiResponse<CourseCreationResponse> createCourse(@RequestBody @Valid CourseCreationRequest request) {
        return ApiResponse.<CourseCreationResponse>builder()
                .result(courseService.createCourse(
                        ParseUUID.normalizeUID(request.getUserUid()),
                        request))
                .build();
    }

    @Operation(
            summary = "Get all lessons of a course (using when user hasn't enrolled in course)"
    )
    @GetMapping("/{courseId}/lessons")
    ApiResponse<List<LessonResponse>> getLessonsByCourseId(@PathVariable("courseId") String courseId) {
        return ApiResponse.<List<LessonResponse>>builder()
                .result(lessonService.getLessonsByCourseId(courseId))
                .build();
    }

    @Operation(
            summary = "Get all lessons and progress of learning lessons in a course (using when user has enrolled in course)"
    )
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

    @Operation(
            summary = "Get a course by id, if userUid is provided, return true if user has enrolled in course"
    )
    @GetMapping("/{courseId}")
    ApiResponse<DetailCourseResponse> getCourseById(@PathVariable("courseId") UUID courseId,
         @RequestParam(name = "userUid", value = "userUid", required = false) String userUid) {
        UUID userUUID = null;
        if (userUid != null) {
            userUUID = ParseUUID.normalizeUID(userUid);
        }
        return ApiResponse.<DetailCourseResponse>builder()
                .result(courseService.getCourseById(courseId, userUUID))
                .build();
    }

    @Operation(
            summary = "Get all courses"
    )
    @GetMapping
    ApiResponse<List<CourseCreationResponse>> getAllCourse() {
        return ApiResponse.<List<CourseCreationResponse>>builder()
                .result(courseService.getAllCourses())
                .build();
    }

    @Operation(
            summary = "Delete a course by id"
    )
    @DeleteMapping("/{courseId}")
    ApiResponse<String> deleteCourseById(@PathVariable("courseId") UUID courseId) {
        courseService.deleteCourseById(courseId);
        return ApiResponse.<String>builder()
                .result("Course has been deleted")
                .build();
    }

    @Operation(
            summary = "Update a course by id"
    )
    @PutMapping("/{courseId}")
    ApiResponse<CourseCreationResponse> updateCourse(@PathVariable("courseId") UUID courseId, @RequestBody CourseUpdateRequest request) {
        return ApiResponse.<CourseCreationResponse>builder()
                .result(courseService.updateCourse(courseId, request))
                .build();
    }

    @Operation(
            summary = "Get all courses that contain keyword in title or description"
    )
    @GetMapping("/search")
    public ApiResponse<List<CourseCreationResponse>> searchCourses(@RequestParam("keyword") String keyword) {
        return ApiResponse.<List<CourseCreationResponse>>builder()
                .result(courseService.searchCourses(keyword))
                .build();
    }

    @Operation(
            summary = "Enroll a course"
    )
    @PostMapping("/enroll")
    public ApiResponse<UserCourses> enrollCourse(@RequestBody @Valid EnrollCourseRequest request) {

        return ApiResponse.<UserCourses>builder()
                .result(courseService.enrollCourse(ParseUUID.normalizeUID(request.getUserUid()), request.getCourseId()))
                .build();
    }

    @Operation(
            summary = "Get all users have enrolled a course"
    )
    @GetMapping("/{courseId}/enrolledUsers")
    public ApiResponse<List<UserCourses>> getEnrolledUsersOfCourse(@PathVariable("courseId") UUID courseId) {
        return ApiResponse.<List<UserCourses>>builder()
                .result(courseService.getEnrolledUsersOfCourse(courseId))
                .build();
    }

    @Operation(
            summary = "Get all courses that a user has enrolled"
    )
    @GetMapping("/{userUid}/enrolledCourses")
    public ApiResponse<List<UserCourses>> getEnrolledCoursesOfUser(@PathVariable("userUid") String userUid) {
        return ApiResponse.<List<UserCourses>>builder()
                .result(courseService.getEnrolledCoursesOfUser(ParseUUID.normalizeUID(userUid)))
                .build();
    }

}
