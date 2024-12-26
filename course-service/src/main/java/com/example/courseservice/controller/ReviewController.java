package com.example.courseservice.controller;

import com.example.courseservice.dto.ApiResponse;
import com.example.courseservice.dto.request.review.ReviewCreationRequest;
import com.example.courseservice.dto.request.review.ReviewUpdateRequest;
import com.example.courseservice.dto.response.rerview.DetailsReviewResponse;
import com.example.courseservice.dto.response.rerview.ReviewCreationResponse;
import com.example.courseservice.service.ReviewService;
import com.example.courseservice.utils.ParseUUID;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Review")
public class ReviewController {
    ReviewService reviewService;

    @Operation(
            summary = "Create review"
    )
    @PostMapping
    public ApiResponse<ReviewCreationResponse> createReview(@RequestBody @Valid ReviewCreationRequest request) {
        return ApiResponse.<ReviewCreationResponse>builder()
                .result(reviewService.createReview(
                            request,
                            ParseUUID.normalizeUID(request.getUserUid())
                        )
                )
                .build();
    }

    @Operation(
            summary = "Get review by id"
    )
    @GetMapping("/{reviewId}")
    public ApiResponse<DetailsReviewResponse> getReviewById(
            @PathVariable("reviewId") UUID reviewId) {
        return ApiResponse.<DetailsReviewResponse>builder()
                .result(
                        reviewService.getReviewById(
                                reviewId
                        )
                )
                .build();
    }

    @Operation(
            summary = "Update review by id"
    )
    @PutMapping("/{reviewId}")
    public ApiResponse<DetailsReviewResponse> updateReviewById(
            @PathVariable("reviewId") UUID reviewId,
            @RequestBody @Valid ReviewUpdateRequest request) {

        return ApiResponse.<DetailsReviewResponse>builder()
                .result(
                        reviewService.updateReviewById(
                                reviewId,
                                request
                        )
                )
                .build();
    }

    @Operation(
            summary = "Delete a review by id"
    )
    @DeleteMapping("/{reviewId}")
    public ApiResponse<Boolean> deleteReviewById(
            @PathVariable("reviewId") UUID reviewId) {
        reviewService.deleteReviewById(reviewId);
        return ApiResponse.<Boolean>builder()
                .result(true)
                .build();
    }


}
