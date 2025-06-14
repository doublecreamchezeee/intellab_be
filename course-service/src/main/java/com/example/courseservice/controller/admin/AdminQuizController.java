package com.example.courseservice.controller.admin;


import com.example.courseservice.dto.ApiResponse;
import com.example.courseservice.dto.request.exercise.ModifyQuizRequest;
import com.example.courseservice.dto.response.exercise.ExerciseResponse;
import com.example.courseservice.exception.AppException;
import com.example.courseservice.exception.ErrorCode;
import com.example.courseservice.service.ExerciseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin/lessons/quiz")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "Admin Lesson")
public class AdminQuizController {
    final String defaultRole = "myRole";
    private Boolean isAdmin(String role)
    {
        return role.contains("admin");
    }

    private final ExerciseService exerciseService;

    @Operation(
            summary = "Modify list Quiz",
            description = "Các case dùng:" +
                    "1. Lesson vừa mới tạo or lesson CHƯA CÓ QUIZ VÀ ĐANG TẮT QUIZ: " +
                    "   - truyền isQuizVisible = true, để bật + danh sách list câu hỏi. Để tạo list câu hỏi và bật quiz visible" +
                    "2. Lesson đã có quiz và đang bật quiz visible: chỉ cần isQuizVisible = false để tắt"
    )
    @PutMapping()
    ApiResponse<ExerciseResponse> modifyQuiz(
            @RequestBody ModifyQuizRequest request,
            @RequestHeader(value = "X-UserRole") String userRole
    ) {
        userRole = userRole.split(",")[0];
        if (!isAdmin(userRole)) {
            throw new AppException(ErrorCode.USER_IS_NOT_ADMIN);
        }

        return ApiResponse.<ExerciseResponse>builder()
                .result(exerciseService.updateQuiz(request))
                .build();
    }

    @Operation(
            summary = "Remove question from quiz",
            description = ""
    )
    @DeleteMapping("/removeQuestion/{questionId}")
    ResponseEntity<Void> deleteQuestion(
            @PathVariable("questionId") UUID questionId,
            @RequestHeader(value = "X-UserRole") String userRole
    ) {
        userRole = userRole.split(",")[0];
        if (!isAdmin(userRole)) {
            throw new AppException(ErrorCode.USER_IS_NOT_ADMIN);
        }

        exerciseService.removeQuestionFromQuiz(questionId);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Get quiz by lesson id",
            description = ""
    )
    @GetMapping("/{lessonId}")
    ApiResponse<ExerciseResponse> getQuiz(
            @PathVariable("lessonId") UUID lessonId,
            @RequestHeader(value = "X-UserRole") String userRole
    ) {
        userRole = userRole.split(",")[0];
        if (!isAdmin(userRole)) {
            throw new AppException(ErrorCode.USER_IS_NOT_ADMIN);
        }
        return ApiResponse.<ExerciseResponse>builder()
                .result(exerciseService.getExerciseByLessonId(lessonId))
                .build();
    }
}
