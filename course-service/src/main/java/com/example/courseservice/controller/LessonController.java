package com.example.courseservice.controller;

import com.example.courseservice.dto.ApiResponse;
import com.example.courseservice.dto.request.exercise.ExerciseCreationRequest;
import com.example.courseservice.dto.request.learningLesson.LearningLessonCreationRequest;
import com.example.courseservice.dto.request.learningLesson.LearningLessonUpdateRequest;
import com.example.courseservice.dto.request.lesson.LessonCreationRequest;
import com.example.courseservice.dto.request.lesson.LessonUpdateRequest;
import com.example.courseservice.dto.response.Question.QuestionResponse;
import com.example.courseservice.dto.response.learningLesson.LearningLessonResponse;
import com.example.courseservice.dto.response.lesson.DetailsLessonResponse;
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
            summary = "Get one lesson by id, provide userUid to check user has enrolled course or not, else 403"
    )
    @GetMapping("/{lessonId}/{userId}")
    ApiResponse<DetailsLessonResponse> getLessonById(@PathVariable("lessonId") String lessonId, @PathVariable("userId") String userId){
        return ApiResponse.<DetailsLessonResponse>builder()
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
            summary = "Update learning progress of lesson by id, status can be 'new', 'inprogress', 'completed' (call before fetch detail lesson)"
    )
    @PutMapping("/{learningLessonId}/{courseId}/{userUid}/updateLearningProgress")
    ApiResponse<LearningLessonResponse> updateLearningProgress(
            @PathVariable("learningLessonId") UUID learningLessonId,
            @PathVariable("courseId") UUID courseId,
            @PathVariable("userUid") String userUid,
            @RequestBody LearningLessonUpdateRequest request) {

        return ApiResponse.<LearningLessonResponse>builder()
                .result(lessonService.updateLearningLesson(learningLessonId, courseId, userUid, request))
                .build();
    }

    @Operation(
            summary = "Add exercise to lesson"
    )
    @PostMapping("/{lessonId}")
    ApiResponse<LessonResponse> addExercise(@PathVariable("lessonId") UUID lessonId, @RequestBody ExerciseCreationRequest request){

        return ApiResponse.<LessonResponse>builder()
                .result(lessonService.addExercise(lessonId,request))
                .build();
    }

    @Operation(
            summary = "Get quiz of lesson"
    )
    @GetMapping("/{lessonId}/quiz")
    ApiResponse<QuestionResponse> quiz(@PathVariable("lessonId") UUID lessonId){
        return ApiResponse.<QuestionResponse>builder()
                .result(lessonService.getQuestion(lessonId))
                .build();
    }

    @Operation(
            summary = "Mark theory of lesson as done"
    )
    @PutMapping("/{learningLessonId}/{courseId}/{userUid}/doneTheory")
    ApiResponse<Boolean> doneTheoryOfLesson(
            @PathVariable("learningLessonId") UUID learningLessonId,
            @PathVariable("courseId") UUID courseId,
            @PathVariable("userUid") String userUid) {

        return ApiResponse.<Boolean>builder()
                .result(lessonService.doneTheoryOfLesson(
                            learningLessonId,
                            courseId,
                            ParseUUID.normalizeUID(userUid)
                        )
                )
                .build();
    }

    @Operation(
            summary = "Mark practice of lesson as done"
    )
    @PutMapping("/{learningLessonId}/{courseId}/{userUid}/donePractice")
    ApiResponse<Boolean> donePracticeOfLesson(
            @PathVariable("learningLessonId") UUID learningLessonId,
            @PathVariable("courseId") UUID courseId,
            @PathVariable("userUid") String userUid) {

        return ApiResponse.<Boolean>builder()
                .result(lessonService.donePracticeOfLesson(
                            learningLessonId,
                            courseId,
                            ParseUUID.normalizeUID(userUid)
                        )
                )
                .build();
    }

}
