package com.example.courseservice.controller;

import com.example.courseservice.dto.ApiResponse;
import com.example.courseservice.dto.request.review.ReviewCreationRequest;
import com.example.courseservice.dto.response.rerview.ReviewCreationResponse;
import com.example.courseservice.service.ReviewService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ReviewController {
    ReviewService reviewService;
    @PostMapping("")
    public ApiResponse<ReviewCreationResponse> createReview(@RequestBody @Valid ReviewCreationRequest request) {
        return ApiResponse.<ReviewCreationResponse>builder()
                .result(reviewService.createReview(request))
                .build();
    }
}
