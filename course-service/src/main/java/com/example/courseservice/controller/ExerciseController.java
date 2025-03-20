package com.example.courseservice.controller;


import com.example.courseservice.dto.ApiResponse;
import com.example.courseservice.dto.request.exercise.ExerciseCreationRequest;
import com.example.courseservice.dto.request.exercise.ModifyQuesstionListRequest;
import com.example.courseservice.dto.response.Question.QuestionResponse;
import com.example.courseservice.dto.response.course.CourseCreationResponse;
import com.example.courseservice.dto.response.exercise.AddQuestionToExerciseResponse;
import com.example.courseservice.dto.response.exercise.ExerciseDetailResponse;
import com.example.courseservice.dto.response.exercise.ExerciseResponse;
import com.example.courseservice.model.Exercise;
import com.example.courseservice.service.ExerciseService;
import com.example.courseservice.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/exercises")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Exercise")
public class ExerciseController {
    ExerciseService exerciseService;
    QuestionService questionService;
    @Operation(
            summary = "Get all exercises"
    )
    @GetMapping
    ApiResponse<List<ExerciseResponse>> getAllExercises() {

        return ApiResponse.<List<ExerciseResponse>>builder()
                .result(exerciseService.getAllExercises())
                .build();
    }

    @Operation(
            summary = "Get exercise by id"
    )
    @GetMapping("/{exerciseId}")
    public ApiResponse<ExerciseDetailResponse> getExerciseById(@PathVariable UUID exerciseId) {
        return ApiResponse.<ExerciseDetailResponse>builder()
                .result(exerciseService.getExerciseById(exerciseId))
                .build();
    }

    @Operation(
            summary = "Create exercise"
    )
    @PostMapping
    public  ApiResponse<ExerciseResponse> createExercise(@RequestBody ExerciseCreationRequest request) {
        return ApiResponse.<ExerciseResponse>builder()
                .result(exerciseService.createExercise(request))
                .build();
    }

    @Operation(
            summary = "Add question to exercise"
    )
    @PostMapping("/{exerciseId}")
    public ApiResponse<List<AddQuestionToExerciseResponse>> addQuestionToExerciseResponseApiResponse(
            @PathVariable UUID exerciseId,
            @RequestBody ModifyQuesstionListRequest request)
    {


        List<AddQuestionToExerciseResponse> responses = new ArrayList<>();
        for (UUID questionId : request.getAddQuestions()) {
            responses.add(exerciseService.addQuestionToExxerciseResponse(exerciseId, questionId));
        }

        for (UUID questionId : request.getRemoveQuestions()) {
            exerciseService.removeQuestionFromExercise(exerciseId, questionId);
        }
        return ApiResponse.<List<AddQuestionToExerciseResponse>>builder()
                .result(responses)
                .build();
    }

    @Operation(
            summary = "Get list questions by exercise id"
    )
    @GetMapping("/{exerciseId}/questions")
    public ApiResponse<List<QuestionResponse>> getQuestionsByExerciseId(
            @PathVariable UUID exerciseId) {
        return ApiResponse.<List<QuestionResponse>>builder()
                .result(questionService.getQuestionsByExerciseId(exerciseId))
                .build();
    }

}
