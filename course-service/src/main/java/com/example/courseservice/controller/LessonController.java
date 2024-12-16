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
public class LessonController {
    LessonService lessonService;
    ExerciseService exerciseService;
//    @PostMapping
//    ApiResponse<LessonResponse> createLesson(@RequestBody @Valid LessonCreationRequest request) {
//        return ApiResponse.<LessonResponse>builder()
//                .result(lessonService.createLesson(request))
//                .build();
//    }

    @PostMapping
    ApiResponse<LessonResponse> createLesson(@RequestBody @Valid LessonCreationRequest request) {
        return ApiResponse.<LessonResponse>builder()
                .result(lessonService.createLesson(request))
                .build();
    }

    @GetMapping("/{lessonId}/{userId}")
    ApiResponse<LessonResponse> getLessonById(@PathVariable("lessonId") String lessonId, @PathVariable("userId") String userId){
        return ApiResponse.<LessonResponse>builder()
                .result(lessonService.getLessonById(
                        lessonId,
                        ParseUUID.normalizeUID(userId))
                )
                .build();
    }

    @DeleteMapping("/{lessonId}")
    ApiResponse<String> deleteLesson(@PathVariable("lessonId") String lessonId){
        lessonService.deleteLesson(lessonId);
        return ApiResponse.<String>builder()
                .result("Lesson has been deleted")
                .build();
    }

    @PutMapping("/{lessonId}")
    ApiResponse<LessonResponse> updateLesson(@PathVariable("lessonId") String lessonId, @RequestBody LessonUpdateRequest request){
        return ApiResponse.<LessonResponse>builder()
                .result(lessonService.updateLesson(lessonId, request))
                .build();
    }

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
