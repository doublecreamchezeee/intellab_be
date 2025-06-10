package com.example.courseservice.controller;


import com.example.courseservice.dto.ApiResponse;
import com.example.courseservice.dto.request.exercise.ModifyQuizRequest;
import com.example.courseservice.dto.response.exercise.ExerciseResponse;
import com.example.courseservice.service.ExerciseService;
import com.example.courseservice.service.OptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/questions")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Question")
public class QuestionController {

    OptionService optionService;
    private final ExerciseService exerciseService;

    @Operation(
            summary = "Modify list Quiz",
            description = "",
            hidden = true
    )
    @PutMapping("/quiz")
    ApiResponse<ExerciseResponse> modifyQuiz(@RequestBody ModifyQuizRequest request) {
        return ApiResponse.<ExerciseResponse>builder()
                .result(exerciseService.updateQuiz(request))
                .build();
    }

    @Operation(
            summary = "delete question"
    )
    @DeleteMapping("/removeQuestion/{questionId}")
    ResponseEntity<Void> deleteQuestion(@PathVariable("questionId") UUID questionId) {
        exerciseService.removeQuestionFromQuiz(questionId);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "get Quiz by lessonId"
    )
    @GetMapping("/{lessonId}/quiz")
    ApiResponse<ExerciseResponse> getQuiz(@PathVariable("lessonId") UUID lessonId) {
        return ApiResponse.<ExerciseResponse>builder()
                .result(exerciseService.getExerciseByLessonId(lessonId))
                .build();
    }


//    @Operation(
//            summary = "Update one option of a question"
//    )
//    @PutMapping("/{questionId}/options/{optionOrder}")
//    ApiResponse<OptionResponse> updateOption(@PathVariable("questionId") UUID questionId,
//                                             @PathVariable("optionOrder") Integer optionOrder,
//                                             @RequestBody OptionRequest optionRequest) {
//        optionRequest.setOrder(optionOrder);
//        OptionResponse response = optionService.updateOption(questionId, optionRequest);
//        return ApiResponse.<OptionResponse>builder().result(response).build();
//    }
//
//    @Operation(
//            summary = "Create option for a question"
//    )
//    @PostMapping("/{questionId})")
//    public ApiResponse<OptionResponse> createOption(@PathVariable("questionId") UUID questionId, @RequestBody OptionRequest request) {
//
//        return ApiResponse.<OptionResponse>builder()
//                .result(optionService.creteOption(questionId,request))
//                .build();
//    }
}
