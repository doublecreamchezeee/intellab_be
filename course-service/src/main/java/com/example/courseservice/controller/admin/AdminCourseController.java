package com.example.courseservice.controller.admin;

import com.example.courseservice.dto.ApiResponse;
import com.example.courseservice.dto.request.course.FinalCourseCreationRequest;
import com.example.courseservice.dto.request.course.GeneralCourseCreationRequest;
import com.example.courseservice.dto.response.course.AdminCourseSearchResponse;
import com.example.courseservice.dto.response.course.AdminCourseCreationResponse;
import com.example.courseservice.dto.response.course.CourseSearchResponse;
import com.example.courseservice.dto.response.lesson.LessonResponse;
import com.example.courseservice.exception.AppException;
import com.example.courseservice.exception.ErrorCode;
import com.example.courseservice.service.CommentService;
import com.example.courseservice.service.CourseService;
import com.example.courseservice.service.LessonService;
import com.example.courseservice.utils.ParseUUID;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin/courses")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "Admin Course")
public class AdminCourseController {
    CourseService courseService;
    LessonService lessonService;
    final String defaultRole = "myRole";
    private Boolean isAdmin(String role)
    {
        return role.contains("admin");
    }

    @Operation(
            summary = "Get course by id"
    )
    @GetMapping("/{courseId}")
    public ApiResponse<AdminCourseCreationResponse> getCourseById(
            @PathVariable("courseId") UUID courseId,
            @RequestHeader(value = "X-UserID", required = true) String userUid,
            @RequestHeader(value = "X-UserRole", required = true) String userRole
    ) {
        userUid = userUid.split(",")[0];
        UUID userUUid = ParseUUID.normalizeUID(userUid);

        return ApiResponse.<AdminCourseCreationResponse>builder()
                .message("Get course successfully")
                .result(courseService.getCourseOfAdminByCourseId(courseId, userUUid, userRole))
                .build();
    }

    @Operation(
            summary = "Get all courses"
    )
    @GetMapping("")
    ApiResponse<Page<AdminCourseCreationResponse>> getAllCourse(
            @RequestHeader(value = "X-UserID", required = true) String userUid,
            @ParameterObject Pageable pageable,
            @RequestParam(required = false) Integer Section,
            @RequestParam(value = "isAvailable", required = false) Boolean isAvailable,
            @RequestParam(value = "isCompletedCreation", required = false) Boolean isCompletedCreation
    ) {
        userUid = userUid.split(",")[0];
        UUID userUuid = ParseUUID.normalizeUID(userUid);

        if (Section != null) {
            return ApiResponse.<Page<AdminCourseCreationResponse>>builder()
                    .result(courseService.getAllCoursesOfAdminByCategory(
                                    Section,
                                    isAvailable,
                                    isCompletedCreation,
                                    userUuid,
                                    pageable
                            )
                    )
                    .build();
        }

        return ApiResponse.<Page<AdminCourseCreationResponse>>builder()
                .result(courseService.getAllCoursesOfAdmin(
                                isAvailable,
                                isCompletedCreation,
                                userUuid,
                                pageable
                        )
                )
                .build();
    }

    @Operation(
            summary = "Get all courses that contain keyword in title or description"
    )
    @GetMapping("/search")
    public ApiResponse<Page<AdminCourseSearchResponse>> searchCourses(
            @RequestHeader(value = "X-UserID", required = false) String userUid,
            @RequestParam("keyword") String keyword,
            @RequestParam(required = false) Float ratings,
            @RequestParam(required = false) List<String> levels,
            @RequestParam(required = false) Boolean price,
            @RequestParam(required = false) List<Integer> categories,
            @RequestParam(value = "isAvailable", required = false) Boolean isAvailable,
            @RequestParam(value = "isCompletedCreation", required = false) Boolean isCompletedCreation,
            @ParameterObject Pageable pageable) {

        UUID normalizedUserId = null;

        if (userUid != null) {
            userUid = userUid.split(",")[0];
            normalizedUserId = ParseUUID.normalizeUID(userUid);
        }

        System.out.println(userUid);
        if (ratings != null || levels != null || price != null || categories != null) {
            return ApiResponse.<Page<AdminCourseSearchResponse>>builder()
                    .result(courseService.searchCoursesOfAdminWithFilter(
                                    normalizedUserId,
                                    keyword, ratings, levels, price, categories, isAvailable, isCompletedCreation,
                                    pageable
                            )
                    ).build();
        }

        return ApiResponse.<Page<AdminCourseSearchResponse>>builder()
                .result(courseService.searchCoursesOfAdmin(
                                normalizedUserId,
                                keyword,
                                isAvailable,
                                isCompletedCreation,
                                pageable
                        )
                )
                .build();
    }

    @Operation(
            summary = "Delete a course by id"
    )
    @DeleteMapping("/{courseId}")
    ApiResponse<String> deleteCourseById(
            @PathVariable("courseId") UUID courseId,
            @RequestHeader("X-UserID") String userUid,
            @RequestHeader("X-UserRole") String role) {
        if (!isAdmin(role)) {
            return ApiResponse.<String>builder()
                    .code(201)
                    .result("You are not allowed to delete this course")
                    .message("Forbidden").build();
        }
        userUid = userUid.split(",")[0];
        courseService.deleteCourseById(courseId, userUid);
        return ApiResponse.<String>builder()
                .code(204)
                .message("Delete course by id: " + courseId)
                .result("Course has been deleted")
                .build();
    }


    @Operation(
            summary = "Create general information in creating a course"
    )
    @PostMapping("/general-step")
    public ApiResponse<AdminCourseCreationResponse> createGeneralStepInCreatingCourse(
            @RequestBody GeneralCourseCreationRequest request,
            @RequestHeader(value = "X-UserId", required = true) String userUid,
            @RequestHeader(value = "X-UserRole", required = true) String userRole
    ) {
        userUid = userUid.split(",")[0];
        UUID userUUid = ParseUUID.normalizeUID(userUid);

        userRole = userRole.split(",")[0];

        return ApiResponse.<AdminCourseCreationResponse>builder()
                .message("Create course successfully")
                .result(courseService.createGeneralStepInCourseCreation(
                                request, userUUid, userRole
                        )
                )
                .build();
    }

    @Operation(
            summary = "Create final step in creating a course",
            description = "Provide price and unit price"
    )
    @PostMapping("/final-step/{courseId}")
    public ApiResponse<AdminCourseCreationResponse> createFinalStepInCreatingCourse(
            @RequestBody FinalCourseCreationRequest request,
            @PathVariable("courseId") UUID courseId,
            @RequestHeader(value = "X-UserId", required = true) String userUid,
            @RequestHeader(value = "X-UserRole", required = true) String userRole
    ) {
        userUid = userUid.split(",")[0];
        UUID userUUid = ParseUUID.normalizeUID(userUid);

        userRole = userRole.split(",")[0];

        return ApiResponse.<AdminCourseCreationResponse>builder()
                .message("Create course successfully")
                .result(courseService.createFinalStepInCourseCreation(
                                request, courseId, userUUid, userRole
                        )
                )
                .build();
    }

    @Operation(
            summary = "Update general information of course"
    )
    @PutMapping("/general-step/{courseId}")
    public ApiResponse<AdminCourseCreationResponse> updateGeneralStepInCreatingCourse(
            @RequestBody GeneralCourseCreationRequest request,
            @PathVariable("courseId") UUID courseId,
            @RequestHeader(value = "X-UserId", required = true) String userUid,
            @RequestHeader(value = "X-UserRole", required = true) String userRole
    ) {
        userUid = userUid.split(",")[0];
        UUID userUUid = ParseUUID.normalizeUID(userUid);

        userRole = userRole.split(",")[0];

        return ApiResponse.<AdminCourseCreationResponse>builder()
                .message("Update course successfully")
                .result(courseService.updateGeneralStepInCourseCreation(
                                request, courseId, userUUid, userRole
                        )
                )
                .build();
    }

    @Operation(
            summary = "Update final step in creating a course",
            description = "Update price"
    )
    @PutMapping("final-step/{courseId}")
    public ApiResponse<AdminCourseCreationResponse> updateFinalStepInCreatingCourse(
            @RequestBody FinalCourseCreationRequest request,
            @PathVariable("courseId") UUID courseId,
            @RequestHeader(value = "X-UserId", required = true) String userUid,
            @RequestHeader(value = "X-UserRole", required = true) String userRole
    ) {
        userUid = userUid.split(",")[0];
        UUID userUUid = ParseUUID.normalizeUID(userUid);

        userRole = userRole.split(",")[0];

        return ApiResponse.<AdminCourseCreationResponse>builder()
                .message("Update course successfully")
                .result(courseService.updateFinalStepInCourseCreation(
                                request, courseId, userUUid, userRole
                        )
                )
                .build();
    }

    @Operation(
            summary = "Update available status of a course"
    )
    @PutMapping("/update-available-status/{courseId}")
    public ApiResponse<AdminCourseCreationResponse> updateAvailableStatusOfCourse(
            @RequestParam(value = "availableStatus", required = true) Boolean availableStatus,
            @PathVariable("courseId") UUID courseId,
            @RequestHeader(value = "X-UserId", required = true) String userUid,
            @RequestHeader(value = "X-UserRole", required = true) String userRole
    ) {
        userUid = userUid.split(",")[0];
        UUID userUUid = ParseUUID.normalizeUID(userUid);

        userRole = userRole.split(",")[0];

        return ApiResponse.<AdminCourseCreationResponse>builder()
                .message("Update course successfully")
                .result(courseService.updateCourseAvailableStatus(
                                availableStatus, courseId, userUUid, userRole
                        )
                )
                .build();
    }

    @Operation(
            summary = "Update course completed creation status by course id"
    )
    @PutMapping("/update-completed-creation-status/{courseId}")
    public ApiResponse<AdminCourseCreationResponse> updateCompletedCreationStatus(
            @RequestParam(value = "completedCreation", required = true) Boolean completedCreation,
            @PathVariable("courseId") UUID courseId,
            @RequestHeader(value = "X-UserId", required = true) String userUid,
            @RequestHeader(value = "X-UserRole", required = true) String userRole
    ) {
        userUid = userUid.split(",")[0];
        UUID userUUid = ParseUUID.normalizeUID(userUid);

        userRole = userRole.split(",")[0];

        return ApiResponse.<AdminCourseCreationResponse>builder()
                .message("Update course successfully")
                .result(courseService.updateCourseCompletedCreationStatus(
                                completedCreation, courseId, userUUid, userRole
                        )
                )
                .build();
    }

    @Operation(
            summary = "Upload course image file"
    )
    @PostMapping(value = "/{courseId}/image/upload-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<String> uploadCourseImage(
            @PathVariable("courseId") UUID courseId,
            @RequestPart(value = "file", required = true) MultipartFile file
    ) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty!");
        }

        return ApiResponse.<String>builder()
                .message("Upload image success!")
                .result(courseService.uploadCourseAvatarImage(file, courseId))
                .build();
    }

    @Operation(
            summary = "Change course image link"
    )
    @PostMapping(value = "/{courseId}/image/link")
    public ApiResponse<String> uploadCourseImageLink(
            @PathVariable("courseId") UUID courseId,
            @RequestBody String link
    ) {
        return ApiResponse.<String>builder()
                .message("Upload image success!")
                .result(courseService.uploadCourseAvatarLink(link, courseId))
                .build();
    }

    @Operation(
            summary = "Delete course image"
    )
    @DeleteMapping(value = "/{courseId}/image")
    public ApiResponse<Boolean> deleteCourseImage(
            @PathVariable("courseId") UUID courseId
    ) {
        return ApiResponse.<Boolean>builder()
                .message("Delete image success!")
                .result(courseService.deleteCourseAvatarImage(courseId))
                .build();
    }

    @Operation()
    @GetMapping("/certificate/template/{templateId}")
    public ResponseEntity<String> generateCertificate(
            @PathVariable Integer templateId,
            @RequestHeader(value = "X-UserRole") String userRole
    ) {
        userRole = userRole.split(",")[0];
        if (!isAdmin(userRole)) {
            throw new AppException(ErrorCode.USER_IS_NOT_ADMIN);
        }
        return ResponseEntity.ok().body(courseService.GetCertificateTemplateExample(templateId));
    }

    @Operation(
            summary = "Get lesson list",
            description = "trả về List thay vì page"
    )
    @GetMapping("/{courseId}/lessonsList")
    ApiResponse<List<LessonResponse>> getCourseLessons(
            @PathVariable("courseId") UUID courseId
    ){
        return ApiResponse.<List<LessonResponse>>builder()
                .result(lessonService.getCourseLessons(courseId))
                .build();
    }

}
