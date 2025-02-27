package com.example.problemservice.controller;

import com.example.problemservice.dto.request.problemComment.ProblemCommentCreationRequest;
import com.example.problemservice.dto.request.problemComment.ProblemCommentUpdateRequest;
import com.example.problemservice.dto.response.ApiResponse;
import com.example.problemservice.dto.response.problemComment.DetailsProblemCommentResponse;
import com.example.problemservice.dto.response.problemComment.ProblemCommentCreationResponse;
import com.example.problemservice.dto.response.problemComment.ProblemCommentUpdateResponse;
import com.example.problemservice.dto.response.problemComment.SingleProblemCommentResponse;
import com.example.problemservice.service.ProblemCommentService;
import com.example.problemservice.utils.ParseUUID;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/problem-comments")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Problem Comment")
public class ProblemCommentController {
    private final ProblemCommentService problemCommentService;

    @Operation(
            summary = "Create a new comment"
    )
    @PostMapping
    public ApiResponse<ProblemCommentCreationResponse> createProblemComment(
            @RequestBody ProblemCommentCreationRequest request,
            @RequestHeader("X-UserId") String userUid
    ) {
        userUid = userUid.split(",")[0];

        return ApiResponse.<ProblemCommentCreationResponse>builder()
                .message("Problem comment created successfully")
                .result(problemCommentService.createProblemComment(userUid, request))
                .build();
    }

    @Operation(
            summary = "Update a comment by comment id"
    )
    @PutMapping("/{commentId}")
    public ApiResponse<ProblemCommentUpdateResponse> updateProblemComment(
            @PathVariable String commentId,
            @RequestBody ProblemCommentUpdateRequest request,
            @RequestHeader("X-UserId") String userUid
    ) {
        userUid = userUid.split(",")[0];

        UUID problemCommentId = UUID.fromString(
                commentId
        );

        return ApiResponse.<ProblemCommentUpdateResponse>builder()
                .message("Problem comment updated successfully")
                .result(problemCommentService.updateProblemComment(userUid, problemCommentId, request))
                .build();
    }

    @Operation(
            summary = "Delete a comment and all its children comments by comment id"
    )
    @DeleteMapping("/{commentId}")
    public ApiResponse<String> deleteProblemComment(
            @PathVariable String commentId,
            @RequestHeader("X-UserId") String userUid
    ) {
        userUid = userUid.split(",")[0];

        UUID problemCommentId = UUID.fromString(
                commentId
        );

        problemCommentService.deleteProblemComment(userUid, problemCommentId);

        return ApiResponse.<String>builder()
                .message("Problem comment deleted successfully")
                .result("success")
                .build();
    }

    @Operation(
            summary = "Soft delete a comment by comment id (hide content of comment but still keep it in database)"
    )
    @DeleteMapping("/{commentId}/soft")
    public ApiResponse<String> softDeleteProblemComment(
            @PathVariable String commentId,
            @RequestHeader("X-UserId") String userUid
    ) {
        userUid = userUid.split(",")[0];

        UUID problemCommentId = UUID.fromString(
                commentId
        );

        problemCommentService.softDeleteProblemComment(userUid, problemCommentId);

        return ApiResponse.<String>builder()
                .message("Problem comment soft deleted successfully")
                .result("success")
                .build();
    }

    @Operation(
            summary = "Get a comment by comment id"
    )
    @GetMapping("/{commentId}")
    public ApiResponse<SingleProblemCommentResponse> getProblemComment(
            @PathVariable String commentId,
            @RequestParam(name = "userId", required = false) String userUid
    ) {
        UUID userUuid = null;

        if (userUid != null) {
            userUid = userUid.split(",")[0];
            userUuid = ParseUUID.normalizeUID(
                    userUid
            );
        }
        return ApiResponse.<SingleProblemCommentResponse>builder()
                .message("Problem comment retrieved successfully")
                .result(
                        problemCommentService.getOneProblemCommentById(
                            commentId,
                            userUuid
                        )
                )
                .build();
    }

    @Operation(
            summary = "Get one comments and its children by comment id"
    )
    @GetMapping("/{parentCommentId}/root-and-children")
    public ApiResponse<DetailsProblemCommentResponse> getProblemCommentAndChildren(
            @PathVariable String parentCommentId,
            @RequestParam(name = "userId", required = false) String userUid,
            @ParameterObject Pageable pageable
    ) {
        UUID userUuid = null;

        if (userUid != null) {
            userUid = userUid.split(",")[0];
            userUuid = ParseUUID.normalizeUID(
                    userUid
            );
        }

        return ApiResponse.<DetailsProblemCommentResponse>builder()
                .message("Problem comment retrieved successfully")
                .result(
                        problemCommentService.getOneProblemCommentAndItsChildrenById(
                                parentCommentId,
                                userUuid,
                                pageable
                        )
                )
                .build();
    }

    @Operation(
            summary = "Get all children comments of a parent problem comment by parent problem id"
    )
    @GetMapping("/{parentCommentId}/children")
    public ApiResponse<Page<DetailsProblemCommentResponse>> getChildrenCommentsOfParentComment(
            @PathVariable String parentCommentId,
            @RequestParam(name = "userId", required = false) String userUid,
            @ParameterObject Pageable pageable
    ) {
        UUID userUuid = null;

        if (userUid != null) {
            userUid = userUid.split(",")[0];
            userUuid = ParseUUID.normalizeUID(
                    userUid
            );
        }

        UUID commentId = UUID.fromString(
                parentCommentId
        );
        return ApiResponse.<Page<DetailsProblemCommentResponse>>builder()
                .message("Problem comment retrieved successfully")
                .result(
                        problemCommentService.getChildrenCommentOfProblemCommentById(
                                commentId,
                                userUuid,
                                pageable
                        )
                )
                .build();
    }

    @Operation(
            summary = "Upvote a comment by comment id, return number of upvote of the comment"
    )
    @PostMapping("/{commentId}/upvote")
    public ApiResponse<Integer> upvoteProblemComment(
            @PathVariable String commentId,
            @RequestHeader("X-UserId") String userUid
    ) {
        userUid = userUid.split(",")[0];

        UUID userUuid = ParseUUID.normalizeUID(
                userUid
        );

        UUID problemCommentId = UUID.fromString(
                commentId
        );

        return ApiResponse.<Integer>builder()
                .message("Problem comment upvoted successfully")
                .result(problemCommentService.upvoteProblemComment(userUuid, problemCommentId))
                .build();
    }

    @Operation(
            summary = "Cancel upvote a comment by comment id, return number of upvote of the comment"
    )
    @PostMapping("/{commentId}/cancel-upvote")
    public ApiResponse<Integer> cancelUpvoteProblemComment(
            @PathVariable String commentId,
            @RequestHeader("X-UserId") String userUid
    ) {
        userUid = userUid.split(",")[0];

        UUID userUuid = ParseUUID.normalizeUID(
                userUid
        );

        UUID problemCommentId = UUID.fromString(
                commentId
        );

        return ApiResponse.<Integer>builder()
                .message("Problem comment upvote canceled successfully")
                .result(problemCommentService.removeUpvoteProblemComment(userUuid, problemCommentId))
                .build();
    }
}
