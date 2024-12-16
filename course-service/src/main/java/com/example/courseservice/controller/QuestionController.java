package com.example.courseservice.controller;


import com.example.courseservice.dto.ApiResponse;
import com.example.courseservice.dto.request.Option.OptionRequest;
import com.example.courseservice.dto.request.Question.QuestionCreationRequest;
import com.example.courseservice.dto.request.Question.QuestionUpdateRequest;
import com.example.courseservice.dto.response.Option.OptionResponse;
import com.example.courseservice.dto.response.Question.QuestionResponse;
import com.example.courseservice.service.OptionService;
import com.example.courseservice.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/questions")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Question")
public class QuestionController {
    QuestionService questionService;
    OptionService optionService;

    @Operation(
            summary = "Get all questions"
    )
    @GetMapping
    public ApiResponse<List<QuestionResponse>> getAllQuestions() {
        return ApiResponse.<List<QuestionResponse>>builder()
                .result(questionService.getAllQuestions())
                .build();
    }

    @Operation(
            summary = "Get question by id"
    )
    @GetMapping("/{questionId}")
    public ApiResponse<QuestionResponse> getQuestion(@PathVariable("questionId") UUID questionId) {
        return ApiResponse.<QuestionResponse>builder()
                .result(questionService.getQuestionById(questionId))
                .build();
    }

    @Operation(
            summary = "Create question"
    )
    @PostMapping
    public ApiResponse<QuestionResponse> createQuestion(@RequestBody QuestionCreationRequest request) {
        return ApiResponse.<QuestionResponse>builder()
                .result(questionService.createQuestion(request))
                .build();
    }

    @Operation(
            summary = "Update question by id"
    )
    @PutMapping("/{questionId}")
    public ApiResponse<QuestionResponse> updateQuestion(@PathVariable("questionId") UUID questionId, @RequestBody QuestionUpdateRequest request) {
        return ApiResponse.<QuestionResponse>builder()
                .result(questionService.updateQuestion(questionId,request))
                .build();
    }

    @Operation(
            summary = "Delete question by id"
    )
    @DeleteMapping("/{questionId}")
    public ApiResponse<String> deleteQuestion(@PathVariable("questionId") UUID questionId) {
        questionService.deleteQuestion(questionId);
        return ApiResponse.<String>builder()
                .result("Question have been deleted.")
                .build();
    }

    // Các control cho option
    @Operation(
            summary = "Delete one option of a question"
    )
    @DeleteMapping("/{questionID}/options/{optionOrder}")
    ApiResponse<String> deleteOption(@PathVariable("questionID") UUID questionId, @PathVariable("optionOrder") int optionOrder) {
        optionService.deleteOption(questionId, optionOrder);
        return ApiResponse.<String>builder().result("Option have been deleted.").build();
    }

    @Operation(
            summary = "Update one option of a question"
    )
    @PutMapping("/{questionId}/options/{optionOrder}")
    ApiResponse<OptionResponse> updateOption(@PathVariable("questionId") UUID questionId,
                                             @PathVariable("optionOrder") Integer optionOrder,
                                             @RequestBody OptionRequest optionRequest) {
        optionRequest.setOrder(optionOrder);
        OptionResponse response = optionService.updateOption(questionId, optionRequest);
        return ApiResponse.<OptionResponse>builder().result(response).build();
    }

    @Operation(
            summary = "Create option for a question"
    )
    @PostMapping("/{questionId})")
    public ApiResponse<OptionResponse> createOption(@PathVariable("questionId") UUID questionId, @RequestBody OptionRequest request) {

        return ApiResponse.<OptionResponse>builder()
                .result(optionService.creteOption(questionId,request))
                .build();
    }
}
