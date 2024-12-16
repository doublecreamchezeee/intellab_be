package com.example.courseservice.controller;


import com.example.courseservice.dto.ApiResponse;
import com.example.courseservice.dto.request.exercise.ExerciseCreationRequest;
import com.example.courseservice.dto.request.exercise.ModifyQuesstionListRequest;
import com.example.courseservice.dto.response.course.CourseCreationResponse;
import com.example.courseservice.dto.response.exercise.AddQuestionToExerciseResponse;
import com.example.courseservice.dto.response.exercise.ExerciseDetailResponse;
import com.example.courseservice.dto.response.exercise.ExerciseResponse;
import com.example.courseservice.model.Exercise;
import com.example.courseservice.service.ExerciseService;
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
public class ExerciseController {

    ExerciseService exerciseService;

    @GetMapping
    ApiResponse<List<ExerciseResponse>> getAllExercises() {

        return ApiResponse.<List<ExerciseResponse>>builder()
                .result(exerciseService.getAllExercises())
                .build();
    }

    @GetMapping("/{exerciseId}")
    public ApiResponse<ExerciseDetailResponse> getExerciseById(@PathVariable UUID exerciseId) {
        return ApiResponse.<ExerciseDetailResponse>builder()
                .result(exerciseService.getExerciseById(exerciseId))
                .build();
    }

    @PostMapping
    public  ApiResponse<ExerciseResponse> createExercise(@RequestBody ExerciseCreationRequest request) {
        return ApiResponse.<ExerciseResponse>builder()
                .result(exerciseService.createExercise(request))
                .build();
    }

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


}
