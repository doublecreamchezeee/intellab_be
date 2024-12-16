package com.example.courseservice.controller;

import com.example.courseservice.dto.ApiResponse;
import com.example.courseservice.dto.request.exercise.ExerciseCreationRequest;
import com.example.courseservice.dto.request.learningLesson.LearningLessonCreationRequest;
import com.example.courseservice.dto.request.learningLesson.LearningLessonUpdateRequest;
import com.example.courseservice.dto.request.lesson.LessonCreationRequest;
import com.example.courseservice.dto.request.lesson.LessonUpdateRequest;
import com.example.courseservice.dto.response.learningLesson.LearningLessonResponse;
import com.example.courseservice.dto.response.lesson.LessonResponse;
import com.example.courseservice.service.ExerciseService;
import com.example.courseservice.model.LearningLesson;
import com.example.courseservice.service.LessonService;
import com.example.courseservice.utils.ParseUUID;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(value = "/lessons")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "Lesson")
public class LessonController {
    LessonService lessonService;

    @Operation(
            summary = "Create lesson"
    )
    @PostMapping
    ApiResponse<LessonResponse> createLesson(@RequestBody @Valid LessonCreationRequest request) {
        return ApiResponse.<LessonResponse>builder()
                .result(lessonService.createLesson(request))
                .build();
    }

    @Operation(
            summary = "Get lesson by id, provide userUid to check user has enrolled course or not, else 403"
    )
    @GetMapping("/{lessonId}/{userId}")
    ApiResponse<LessonResponse> getLessonById(@PathVariable("lessonId") String lessonId, @PathVariable("userId") String userId){
        return ApiResponse.<LessonResponse>builder()
                .result(lessonService.getLessonById(
                        lessonId,
                        ParseUUID.normalizeUID(userId))
                )
                .build();
    }

    @Operation(
            summary = "Delete lesson by id"
    )
    @DeleteMapping("/{lessonId}")
    ApiResponse<String> deleteLesson(@PathVariable("lessonId") String lessonId){
        lessonService.deleteLesson(lessonId);
        return ApiResponse.<String>builder()
                .result("Lesson has been deleted")
                .build();
    }

    @Operation(
            summary = "Update lesson by id"
    )
    @PutMapping("/{lessonId}")
    ApiResponse<LessonResponse> updateLesson(@PathVariable("lessonId") String lessonId, @RequestBody LessonUpdateRequest request){
        return ApiResponse.<LessonResponse>builder()
                .result(lessonService.updateLesson(lessonId, request))
                .build();
    }

    @Operation(
            summary = "Start learning lesson (fe don't need to use, be auto created when user enroll course)"
    )
    @PostMapping("/startLesson")
    ApiResponse<LearningLessonResponse> startLesson(@RequestBody LearningLessonCreationRequest request){
        return ApiResponse.<LearningLessonResponse>builder()
                .result(
                        lessonService.createLearningLesson(
                        ParseUUID.normalizeUID(request.getUserId()),
                        request)
                )
                .build();
    }

    @Operation(
            summary = "Update learning progress of lesson by id, status can be 'completed', 'in-progress', 'not-started'"
    )
    @PutMapping("/{learningLessonId}/updateLearningProgress")
    ApiResponse<LearningLessonResponse> updateLearningProgress(
            @PathVariable("learningLessonId") String learningLessonId,
            @RequestBody LearningLessonUpdateRequest request) {

        return ApiResponse.<LearningLessonResponse>builder()
                .result(lessonService.updateLearningLesson(learningLessonId, request))
                .build();
    }

    @PostMapping("/{lessonId}")
    ApiResponse<LessonResponse> addExercise(@PathVariable("lessonId") UUID lessonId, @RequestBody ExerciseCreationRequest request){

        return ApiResponse.<LessonResponse>builder()
                .result(lessonService.addExercise(lessonId,request))
                .build();
    }

}
