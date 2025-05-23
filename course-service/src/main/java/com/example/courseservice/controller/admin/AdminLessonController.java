package com.example.courseservice.controller.admin;


import com.example.courseservice.dto.ApiResponse;
import com.example.courseservice.dto.request.lesson.LessonCreationRequest;
import com.example.courseservice.dto.request.lesson.LessonUpdateRequest;
import com.example.courseservice.dto.request.lesson.UpdateOrderRequest;
import com.example.courseservice.dto.response.lesson.LessonResponse;
import com.example.courseservice.exception.AppException;
import com.example.courseservice.exception.ErrorCode;
import com.example.courseservice.service.CourseService;
import com.example.courseservice.service.LessonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin/lessons")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "Admin Lesson")
public class AdminLessonController {
    CourseService courseService;
    LessonService lessonService;
    final String defaultRole = "myRole";
    private Boolean isAdmin(String role)
    {
        return role.contains("admin");
    }

    @Operation(
            summary = "Create blank lesson OR clone lesson by id"
    )
    @PostMapping
    public ApiResponse<LessonResponse> createLesson(
            @RequestBody LessonCreationRequest request,
            @RequestHeader(value = "X-UserRole") String userRole
    ) {
        userRole = userRole.split(",")[0];
        if (!isAdmin(userRole)) {
            throw new AppException(ErrorCode.USER_IS_NOT_ADMIN);
        }
        if (request.getClonedLessonId() == null)
        {
            return ApiResponse.<LessonResponse>builder()
                    .result(lessonService.createBlankLesson(request.getCourseId()))
                    .build();
        }
        return ApiResponse.<LessonResponse>builder()
                .result(lessonService.copyLesson(request.getClonedLessonId(),request.getCourseId()))
                .build();
    }


    @Operation(
            summary = "Update lesson by id"
    )
    @PutMapping
    ApiResponse<LessonResponse> updateLesson(
            @RequestBody LessonUpdateRequest request,
            @RequestHeader(value = "X-UserRole") String userRole
    ) {
        userRole = userRole.split(",")[0];
        if (!isAdmin(userRole)) {
            throw new AppException(ErrorCode.USER_IS_NOT_ADMIN);
        }
        return ApiResponse.<LessonResponse>builder()
                .result(lessonService.updateLesson(request))
                .build();
    }

    @Operation(
            summary = "Update lessonOrder"
    )
    @PutMapping("/lessonsOrder")
    ApiResponse<Boolean> updateLessonsOrder(
            @RequestBody UpdateOrderRequest request,
            @RequestHeader(value = "X-UserRole") String userRole
    ) {
        userRole = userRole.split(",")[0];
        if (!isAdmin(userRole)) {
            throw new AppException(ErrorCode.USER_IS_NOT_ADMIN);
        }
        return ApiResponse.<Boolean>builder()
                .result(lessonService.updateLessonsOrder(request.getCourseId(),request.getLessonIds()))
                .build();
    }

    @Operation(
            summary = "Delete lesson by id"
    )
    @DeleteMapping("/{lessonId}")
    ApiResponse<String> deleteLesson(
            @PathVariable("lessonId") UUID lessonId,
            @RequestHeader(value = "X-UserRole") String userRole
    ) {
        userRole = userRole.split(",")[0];
        if (!isAdmin(userRole)) {
            throw new AppException(ErrorCode.USER_IS_NOT_ADMIN);
        }

        lessonService.removeLesson(lessonId);
        return ApiResponse.<String>builder()
                .result("Lesson has been deleted")
                .build();
    }

    @Operation(
            summary = "Get one lesson by id"
    )
    @GetMapping("/{lessonId}")
    ApiResponse<LessonResponse> getLesson(
            @PathVariable("lessonId") UUID lessonId,
            @RequestHeader(value = "X-UserRole") String userRole
    ) {

        if (!isAdmin(userRole)) {
            throw new AppException(ErrorCode.USER_IS_NOT_ADMIN);
        }

        return ApiResponse.<LessonResponse>builder()
                .result(lessonService.getLessonInformation(lessonId))
                .build();
    }
}
