package com.example.courseservice.controller;


import com.example.courseservice.dto.ApiResponse;
import com.example.courseservice.dto.request.Option.OptionRequest;
import com.example.courseservice.dto.request.Question.QuestionCreationRequest;
import com.example.courseservice.dto.request.Question.QuestionUpdateRequest;
import com.example.courseservice.dto.response.Option.OptionResponse;
import com.example.courseservice.dto.response.Question.QuestionResponse;
import com.example.courseservice.service.OptionService;
import com.example.courseservice.service.QuestionService;
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
public class QuestionController {
    QuestionService questionService;
    OptionService optionService;

    @GetMapping
    public ApiResponse<List<QuestionResponse>> getAllQuestions() {
        return ApiResponse.<List<QuestionResponse>>builder()
                .result(questionService.getAllQuestions())
                .build();
    }

    @GetMapping("/{questionId}")
    public ApiResponse<QuestionResponse> getQuestion(@PathVariable("questionId") UUID questionId) {
        return ApiResponse.<QuestionResponse>builder()
                .result(questionService.getQuestionById(questionId))
                .build();
    }

    @PostMapping
    public ApiResponse<QuestionResponse> createQuestion(@RequestBody QuestionCreationRequest request) {
        return ApiResponse.<QuestionResponse>builder()
                .result(questionService.createQuestion(request))
                .build();
    }

    @PutMapping("/{questionId}")
    public ApiResponse<QuestionResponse> updateQuestion(@PathVariable("questionId") UUID questionId, @RequestBody QuestionUpdateRequest request) {
        return ApiResponse.<QuestionResponse>builder()
                .result(questionService.updateQuestion(questionId,request))
                .build();
    }

    @DeleteMapping("/{questionId}")
    public ApiResponse<String> deleteQuestion(@PathVariable("questionId") UUID questionId) {
        questionService.deleteQuestion(questionId);
        return ApiResponse.<String>builder()
                .result("Question have been deleted.")
                .build();
    }

    // Các control cho option
    @DeleteMapping("/{questionID}/options/{optionOrder}")
    ApiResponse<String> deleteOption(@PathVariable("questionID") UUID questionId, @PathVariable("optionOrder") int optionOrder) {
        optionService.deleteOption(questionId, optionOrder);
        return ApiResponse.<String>builder().result("Option have been deleted.").build();
    }

    @PutMapping("/{questionId}/options/{optionOrder}")
    ApiResponse<OptionResponse> updateOption(@PathVariable("questionId") UUID questionId,
                                             @PathVariable("optionOrder") Integer optionOrder,
                                             @RequestBody OptionRequest optionRequest) {
        optionRequest.setOrder(optionOrder);
        OptionResponse response = optionService.updateOption(questionId, optionRequest);
        return ApiResponse.<OptionResponse>builder().result(response).build();
    }

    @PostMapping("/{questionId})")
    public ApiResponse<OptionResponse> createOption(@PathVariable("questionId") UUID questionId, @RequestBody OptionRequest request) {

        return ApiResponse.<OptionResponse>builder()
                .result(optionService.creteOption(questionId,request))
                .build();
    }
}
