package com.example.problemservice.controller;

import com.example.problemservice.dto.request.solution.SolutionCreationRequest;
import com.example.problemservice.dto.request.solution.SolutionUpdateRequest;
import com.example.problemservice.dto.response.ApiResponse;
import com.example.problemservice.dto.response.solution.DetailsSolutionResponse;
import com.example.problemservice.dto.response.solution.SolutionCreationResponse;
import com.example.problemservice.dto.response.solution.SolutionUpdateResponse;
import com.example.problemservice.service.SolutionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/solutions")
@RequiredArgsConstructor
@Tag(name = "Solution")
@Slf4j
public class SolutionController {
    private final SolutionService solutionService;

    @Operation(
            summary = "Create solution"
    )
    @PostMapping
    public ApiResponse<SolutionCreationResponse> createSolution(SolutionCreationRequest request) {
        return ApiResponse.<SolutionCreationResponse>builder()
                .result(solutionService.createSolution(request))
                .message("Solution created successfully")
                .code(201)
                .build();
    }

    @Operation(
            summary = "Update solution by author id and problem id"
    )
    @PutMapping("/update/{problemId}/me")
    public ApiResponse<SolutionUpdateResponse> updateSolution(
            @PathVariable("problemId") String problemId,
            @RequestHeader("X-UserId") String userUid,
            //@PathVariable("authorId") String authorId,
            SolutionUpdateRequest request) {

        String authorId = userUid.split(",")[0];

        return ApiResponse.<SolutionUpdateResponse>builder()
                .result(solutionService.updateSolution(problemId, authorId, request))
                .message("Solution updated successfully")
                .code(200)
                .build();
    }

    @Operation(
            summary = "Get solution by author id and problem id"
    )
    @GetMapping("/{problemId}/{authorId}")
    public ApiResponse<DetailsSolutionResponse> getSolution(
            @PathVariable("problemId") String problemId,
            @PathVariable("authorId") String authorId) {

        return ApiResponse.<DetailsSolutionResponse>builder()
                .result(solutionService.getSolution(problemId, authorId))
                .message("Solution retrieved successfully")
                .code(200)
                .build();
    }

    @Operation(
            summary = "Delete solution by author id and problem id"
    )
    @DeleteMapping("/{problemId}/me")
    public ApiResponse<Boolean> deleteSolution(
            @PathVariable("problemId") String problemId,
            @RequestHeader("X-UserId") String userUid
            //@PathVariable("authorId") String authorId
    ) {
        String authorId = userUid.split(",")[0];
        solutionService.deleteSolution(problemId, authorId);

        return ApiResponse.<Boolean>builder()
                .result(true)
                .code(200)
                .message("Solution deleted successfully")
                .build();
    }



}
