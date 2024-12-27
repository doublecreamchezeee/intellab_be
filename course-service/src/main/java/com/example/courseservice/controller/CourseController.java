package com.example.courseservice.controller;

import com.example.courseservice.dto.ApiResponse;
import com.example.courseservice.dto.request.course.CourseCreationRequest;
import com.example.courseservice.dto.request.course.CourseUpdateRequest;
import com.example.courseservice.dto.request.course.EnrollCourseRequest;
import com.example.courseservice.dto.response.course.CourseCreationResponse;
import com.example.courseservice.dto.response.course.DetailCourseResponse;
import com.example.courseservice.dto.response.learningLesson.LessonProgressResponse;
import com.example.courseservice.dto.response.lesson.LessonResponse;
import com.example.courseservice.dto.response.rerview.DetailsReviewResponse;
import com.example.courseservice.dto.response.userCourses.EnrolledCourseResponse;
import com.example.courseservice.model.Course;
import com.example.courseservice.model.UserCourses;
import com.example.courseservice.service.CourseService;
import com.example.courseservice.service.LessonService;
import com.example.courseservice.service.ReviewService;
import com.example.courseservice.utils.ParseUUID;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "Course")
public class CourseController {
    CourseService courseService;
    LessonService lessonService;
    ReviewService reviewService;

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
    ApiResponse<Page<LessonResponse>> getLessonsByCourseId(
            @PathVariable("courseId") String courseId, @ParameterObject Pageable pageable) {
        return ApiResponse.<Page<LessonResponse>>builder()
                .result(lessonService.getLessonsByCourseId(
                            courseId,
                            pageable
                        )
                )
                .build();
    }

    @Operation(
            summary = "Get all lessons and progress of learning lessons in a course (using when user has enrolled in course)"
    )
    @GetMapping("/{courseId}/{userUid}/lessons")
    ApiResponse<List<LessonProgressResponse>> getLessonProgressByCourseIdAndUserUid(
            @PathVariable("courseId") String courseId, @PathVariable("userUid") String userUid) {
        return ApiResponse.<List<LessonProgressResponse>>builder()
                .result(lessonService.getLessonProgress(
                            ParseUUID.normalizeUID(userUid),
                            UUID.fromString(courseId)
                        )
                )
                .build();
    }

    @Operation(
            summary = "Get a course by id, if userUid is provided, return isUserEnrolled is true if user has enrolled in course"
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
    @GetMapping("")
    ApiResponse<Page<CourseCreationResponse>> getAllCourse(@ParameterObject Pageable pageable) {

        return ApiResponse.<Page<CourseCreationResponse>>builder()
                .result(courseService.getAllCourses(
                            pageable
                        )
                )
                .build();
    }

    @Operation(
            summary = "Get details of multiple courses by their IDs"
    )
    @PostMapping("/details")
    ApiResponse<List<DetailCourseResponse>> getDetailsOfMultipleCourses(
            @RequestBody Map<String, List<String>> requestBody,
            @RequestParam(name = "userUid", required = false) String userUid) {

        UUID userUUID = null;
        if (userUid != null) {
            userUUID = ParseUUID.normalizeUID(userUid);
        }

        // Retrieve the list of course IDs from the map
        List<String> courseIdsList = requestBody.get("courseIds");

        // Convert the list of course IDs (String) to UUID
        List<UUID> courseUUIDs = courseIdsList.stream()
                .map(UUID::fromString)  // Convert each course ID string to UUID
                .collect(Collectors.toList());

        // Call the service to get the course details
        return ApiResponse.<List<DetailCourseResponse>>builder()
                .result(courseService.getDetailsOfCourses(courseUUIDs, userUUID))
                .build();
    }

    @Operation(
            summary = "Get all courses except enrolled courses by user"
    )
    @GetMapping("/exceptEnrolled")
    ApiResponse<Page<CourseCreationResponse>> getAllCourseExceptEnrolledByUser(
            @RequestParam(name = "userUid", value = "userUid", required = false) String userUid, @ParameterObject Pageable pageable) {
        return ApiResponse.<Page<CourseCreationResponse>>builder()
                .result(courseService.getAllCoursesExceptEnrolledByUser(
                        userUid == null ? null : ParseUUID.normalizeUID(userUid),
                        pageable
                ))
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
    public ApiResponse<Page<CourseCreationResponse>> searchCourses(
            @RequestParam("keyword") String keyword, @ParameterObject Pageable pageable) {
        return ApiResponse.<Page<CourseCreationResponse>>builder()
                .result(courseService.searchCourses(
                            keyword, pageable
                        )
                )
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
    public ApiResponse<List<EnrolledCourseResponse>> getEnrolledUsersOfCourse(@PathVariable("courseId") UUID courseId) {
        return ApiResponse.<List<EnrolledCourseResponse>>builder()
                .result(courseService.getEnrolledUsersOfCourse(courseId))
                .build();
    }

    @Operation(
            summary = "Get all courses that a user has enrolled"
    )
    @GetMapping("/{userUid}/enrolledCourses")
    public ApiResponse<Page<UserCourses>> getEnrolledCoursesOfUser(
            @PathVariable("userUid") String userUid,
            @ParameterObject Pageable pageable) {

        return ApiResponse.<Page<UserCourses>>builder()
                .result(courseService.getEnrolledCoursesOfUser(
                            ParseUUID.normalizeUID(userUid),
                            pageable
                        )
                )
                .build();
    }

    @Operation(
            summary = "Get all review of a course by course id"
    )
    @GetMapping("/{courseId}/reviews")
    public ApiResponse<Page<DetailsReviewResponse>> getReviewsByCourseId(
            @PathVariable("courseId") UUID courseId,
            @ParameterObject Pageable pageable) {

        return ApiResponse.<Page<DetailsReviewResponse>>builder()
                .result(reviewService.getAllReviewsByCourseId(courseId, pageable))
                .build();
    }
}
