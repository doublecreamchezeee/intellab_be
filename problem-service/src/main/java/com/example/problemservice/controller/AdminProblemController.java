package com.example.problemservice.controller;

import com.example.problemservice.dto.request.TestCaseCreationRequest;
import com.example.problemservice.dto.request.problem.ProblemCreationRequest;
import com.example.problemservice.dto.request.solution.SolutionCreationRequest;
import com.example.problemservice.dto.request.testcase.TestCaseMultipleCreationRequest;
import com.example.problemservice.dto.response.ApiResponse;
import com.example.problemservice.dto.response.Problem.CategoryResponse;
import com.example.problemservice.dto.response.Problem.ProblemCreationResponse;
import com.example.problemservice.dto.response.Problem.ProblemRowResponse;
import com.example.problemservice.dto.response.solution.SolutionCreationResponse;
import com.example.problemservice.dto.response.testcase.TestCaseCreationResponse;
import com.example.problemservice.service.ProblemService;
import com.example.problemservice.service.SolutionService;
import com.example.problemservice.service.TestCaseService;
import com.example.problemservice.utils.ParseUUID;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin/problems")
@RequiredArgsConstructor
@Tag(name = "Problem")
@Slf4j
public class AdminProblemController {
    private final ProblemService problemService;
    private final TestCaseService testCaseService;
    private final SolutionService solutionService;

    private Boolean isAdmin(String role) {
        return role.contains("admin");
    }

    @Operation(summary = "Create problem")
    @PostMapping
    public ApiResponse<ProblemCreationResponse> createProblem(
            @RequestHeader("X-UserRole") String role,
            @RequestBody ProblemCreationRequest problem) {
        if (!isAdmin(role)) {
            return ApiResponse.<ProblemCreationResponse>builder()
                    .result(null)
                    .code(403)
                    .message("Forbidden").build();
        }
        return ApiResponse.<ProblemCreationResponse>builder()
                .result(problemService.createProblem(problem))
                .code(200)
                .message("Created")
                .build();
    }

    @Operation(
            summary = "Get problems list"
    )
    @GetMapping
    public ApiResponse<Page<ProblemCreationResponse>> getProblem(
            @RequestParam(value = "isComplete", required = true) Boolean isComplete,
            @RequestHeader(value = "X-UserRole", required = true) String role,
            @ParameterObject Pageable pageable
    ) {
        if (!isAdmin(role)) {
            return ApiResponse.<Page<ProblemCreationResponse>>builder()
                    .result(null)
                    .code(403)
                    .message("Forbidden").build();
        }
        return ApiResponse.<Page<ProblemCreationResponse>>builder()
                .message("Get problems list success")
                .result(problemService.getCompleteCreationProblem(
                            isComplete, pageable
                        )
                )
                .build();
    }

    @Operation(
            summary = "Get problems list"
    )
    @GetMapping
    public ApiResponse<List<ProblemRowResponse>> getPrivateProblem(
            @RequestHeader(value = "X-UserRole", required = true) String role,
            @RequestHeader(value = "X-UserId", required = true) String userUid,
            @ParameterObject Pageable pageable
    ) {
        userUid = userUid.split(",")[0];
        UUID userId = ParseUUID.normalizeUID(userUid);

        if (!isAdmin(role)) {
            return ApiResponse.<List<ProblemRowResponse>>builder()
                    .result(null)
                    .code(403)
                    .message("Forbidden").build();
        }
        return ApiResponse.<List<ProblemRowResponse>>builder()
                .message("Get problems list success")
                .result(problemService.getPrivateProblem(
                                userId
                        )
                )
                .build();
    }

    @Operation(summary = "Create general")
    @PostMapping("/general-step")
    public ApiResponse<ProblemCreationResponse> createProblemGeneralStep(
            @RequestHeader("X-UserRole") String role,
            @RequestBody ProblemCreationRequest problem) {
        if (!isAdmin(role)) {
            return ApiResponse.<ProblemCreationResponse>builder()
                    .result(null)
                    .code(403)
                    .message("Forbidden").build();
        }
        return ApiResponse.<ProblemCreationResponse>builder()
                .result(problemService.generalStep(problem))
                .code(200)
                .message("Created")
                .build();
    }

    @Operation(summary = "Create description")
    @PostMapping("/description-step")
    public ApiResponse<ProblemCreationResponse> createProblemDescriptionStep(
            @RequestHeader("X-UserRole") String role,
            @RequestBody ProblemCreationRequest problem) {
        if (!isAdmin(role)) {
            return ApiResponse.<ProblemCreationResponse>builder()
                    .result(null)
                    .code(403)
                    .message("Forbidden").build();
        }
        return ApiResponse.<ProblemCreationResponse>builder()
                .result(problemService.descriptionStep(problem))
                .code(200)
                .message("Created")
                .build();
    }

    @Operation(summary = "Create structure")
    @PostMapping("/structure-step")
    public ApiResponse<ProblemCreationResponse> createProblemStructureStep(
            @RequestHeader("X-UserRole") String role,
            @RequestBody ProblemCreationRequest problem) {
        if (!isAdmin(role)) {
            return ApiResponse.<ProblemCreationResponse>builder()
                    .result(null)
                    .code(403)
                    .message("Forbidden").build();
        }
        return ApiResponse.<ProblemCreationResponse>builder()
                .result(problemService.structureStep(problem))
                .code(200)
                .message("Created")
                .build();
    }

    @Operation(summary = "Create test case")
    @PostMapping("/testcase-step")
    public ApiResponse<TestCaseCreationResponse> createTestCaseStep(
            @RequestBody TestCaseCreationRequest request,
            @RequestHeader("X-UserRole") String role) {
        if (!isAdmin(role)) {
            return ApiResponse.<TestCaseCreationResponse>builder()
                    .result(null)
                    .code(403)
                    .message("Forbidden").build();
        }
        TestCaseCreationResponse response = testCaseService.createTestCase(
                request);
        return ApiResponse.<TestCaseCreationResponse>builder()
                .result(response)
                .code(200)
                .message("Created")
                .build();
    }

    @Operation(summary = "Create test case")
    @PostMapping("/multiple-testcase-step")
    public ApiResponse<List<TestCaseCreationResponse>> createMultipleTestCaseStep(
            @RequestBody TestCaseMultipleCreationRequest request,
            @RequestHeader("X-UserRole") String role) {
        if (!isAdmin(role)) {
            return ApiResponse.<List<TestCaseCreationResponse>>builder()
                    .result(null)
                    .code(403)
                    .message("Forbidden").build();
        }
        List<TestCaseCreationResponse> response = testCaseService.createMultipleTestCases(
                request);
        return ApiResponse.<List<TestCaseCreationResponse>>builder()
                .result(response)
                .code(200)
                .message("Created")
                .build();
    }


    @Operation(
            summary = "Create solution"
    )
    @PostMapping("/solution-step")
    public ApiResponse<SolutionCreationResponse> createSolutionStep(
            @RequestHeader("X-UserRole") String role,
            @RequestBody  SolutionCreationRequest request) {
        if (!isAdmin(role)) {
            return ApiResponse.<SolutionCreationResponse>builder()
                    .result(null)
                    .code(403)
                    .message("Forbidden").build();
        }

        return ApiResponse.<SolutionCreationResponse>builder()
                .result(solutionService.createSolution(request))
                .message("Solution created successfully")
                .code(201)
                .build();
    }

    @GetMapping("/categories")
    public ApiResponse<List<CategoryResponse>> getCategories(){
        return ApiResponse.<List<CategoryResponse>>builder()
                .result(problemService.getCategories())
                .build();
    }

    @Operation(
            summary = "Update available status of a problem"
    )
    @PutMapping("/update-available-status/{problemId}")
    public ApiResponse<ProblemCreationResponse> updateAvailableStatusOfCourse(
            @RequestParam(value = "availableStatus", required = true) Boolean availableStatus,
            @PathVariable("problemId") UUID problemId,
            @RequestHeader(value = "X-UserRole", required = true) String role
    ) {
        if (!isAdmin(role)) {
            return ApiResponse.<ProblemCreationResponse>builder()
                    .result(null)
                    .code(403)
                    .message("Forbidden").build();
        }

        return ApiResponse.<ProblemCreationResponse>builder()
                .message("Update course successfully")
                .result(problemService.updateCourseAvailableStatus(
                                availableStatus, problemId
                        )
                )
                .build();
    }

    @Operation(
            summary = "Update problem completed creation status by problem id"
    )
    @PutMapping("/update-completed-creation-status/{problemId}")
    public ApiResponse<ProblemCreationResponse> updateCompletedCreationStatus(
            @RequestParam(value = "completedCreation", required = true) Boolean completedCreation,
            @PathVariable("problemId") UUID problemId,
            @RequestHeader(value = "X-UserRole", required = true) String role

    ) {
        if (!isAdmin(role)) {
            return ApiResponse.<ProblemCreationResponse>builder()
                    .result(null)
                    .code(403)
                    .message("Forbidden").build();
        }
        return ApiResponse.<ProblemCreationResponse>builder()
                .message("Update course successfully")
                .result(problemService.updateCourseCompletedCreationStatus(
                                completedCreation, problemId
                        )
                )
                .build();
    }


}
