package com.example.problemservice.controller;

import com.example.problemservice.dto.request.DefaultCodeRequest;
import com.example.problemservice.dto.request.problem.EnrichCodeRequest;
import com.example.problemservice.dto.request.problem.ProblemCreationRequest;
import com.example.problemservice.dto.response.ApiResponse;
import com.example.problemservice.dto.response.DefaultCode.DefaultCodeResponse;
import com.example.problemservice.dto.response.DefaultCode.PartialBoilerplateResponse;
import com.example.problemservice.dto.response.Problem.ProblemCreationResponse;
import com.example.problemservice.dto.response.Problem.ProblemRowResponse;
import com.example.problemservice.dto.response.solution.DetailsSolutionResponse;
import com.example.problemservice.model.Problem;
import com.example.problemservice.service.ProblemService;
import com.example.problemservice.exception.AppException;
import com.example.problemservice.exception.ErrorCode;
import com.example.problemservice.utils.ParseUUID;
import com.example.problemservice.service.SolutionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/problems")
@RequiredArgsConstructor
@Tag(name = "Problem")
public class ProblemController {
    private final ProblemService problemService;
    private final SolutionService solutionService;

    @Operation(
            summary = "Create problem"
    )
    @PostMapping
    public ResponseEntity<ProblemCreationResponse> createProblem(@RequestBody ProblemCreationRequest problem) {
        return ResponseEntity.ok(problemService.createProblem(problem));
    }

    @Operation(
            summary = "Get problem by id"
    )
    @GetMapping("/{problemId}")
    public ResponseEntity<Problem> getProblem(@PathVariable UUID problemId) {
        try {
            Problem problem = problemService.getProblem(problemId);
            return ResponseEntity.ok(problem); // Return the problem if found
        } catch (AppException e) {
            if (e.getErrorCode() == ErrorCode.PROBLEM_NOT_EXIST) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // Return 404 if problem not found
            }
            throw e;
        }
    }

    @Operation
    (
            summary = "Get problem page"
    )
    @GetMapping("/search")
    public ApiResponse<Page<ProblemRowResponse>> getProblems(@RequestParam(required = false) String category,
                                                             @RequestHeader(required = false, name = "X-UserID") String userUId,
                                                             @ParameterObject Pageable pageable,
                                                             @RequestParam(required = false) String keyword) {
        if (userUId != null) {
            userUId = userUId.split(",")[0];
            UUID userId = ParseUUID.normalizeUID(userUId);
            System.out.println("here!!!");
            if(keyword != null) {
                System.out.println("here!!");
                return ApiResponse.<Page<ProblemRowResponse>>builder()
                        .result(problemService.searchProblems(pageable,keyword, userId)).build();
            }

            return ApiResponse.<Page<ProblemRowResponse>>builder()
                    .result(problemService.getAllProblems(category, pageable)).build();
        }
        System.out.println("here!!!!");
        if(keyword != null) {
            return ApiResponse.<Page<ProblemRowResponse>>builder()
                    .result(problemService.searchProblems(pageable,keyword)).build();
        }
        return ApiResponse.<Page<ProblemRowResponse>>builder()
                .result(problemService.getAllProblems(category, pageable)).build();
    }

    @Operation(
            summary = "Get all problems"
    )
    @GetMapping("")
    public ResponseEntity<List<Problem>> getAllProblem() {
        try {
            List<Problem> response = problemService.getAllProblems();
            return ResponseEntity.ok(response);
        } catch (AppException e) {
            if (e.getErrorCode() == ErrorCode.PROBLEM_NOT_EXIST) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // Return 404 if problem not found
            }
            throw e;
        }
    }

    @Operation(
            summary = "Delete problem"
    )
    @DeleteMapping("/{problemId}")
    public ResponseEntity<Void> deleteProblem(@PathVariable UUID problemId) {
        problemService.deleteProblem(problemId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Update problem"
    )
    @PutMapping("/{problemId}")
    public ResponseEntity<ProblemCreationResponse> updateProblem(@PathVariable UUID problemId, @RequestBody ProblemCreationRequest request) {
        return ResponseEntity.ok(problemService.updateProblem(problemId, request));
    }


    @Operation(
            summary = "(testing only) Generate boilerplate code"
    )
    @PostMapping("/boilerplateGenerate")
    public ApiResponse<List<DefaultCodeResponse>> generateBoilerPlate(@RequestBody DefaultCodeRequest request) {
        return ApiResponse.<List<DefaultCodeResponse>>builder()
                .result(problemService.generateDefaultCodes(request.getProblemId(),request.getStructure()))
                .build();

    }


    @Operation(
            summary = "Run in postman to generate boilerplate code"
    )
    @PostMapping("/generateBoilerplate")
    public ApiResponse<Boolean> generateBoilerPlate() {
        problemService.generateBoilerplate();
        return ApiResponse.<Boolean>builder()
                .result(true)
                .build();
    }

    // api để test code
    @Operation(
            summary = "(testing only) Enrich code"
    )
    @PostMapping("/enrichCode")
    public ApiResponse<String> enrichCode(@RequestBody EnrichCodeRequest request) {
        // để test
        request.setStructure("Problem Name: \"Sum of Two Numbers\"\n" +
                "Function Name: sum\n" +
                "Input Structure:\n" +
                "Input Field: int num1\n" +
                "Input Field: int num2\n" +
                "Output Structure:\n" +
                "Output Field: int result");
        return ApiResponse.<String>builder()
                .result(problemService.enrichCode(
                        request.getStructure(),
                        request.getCode(),
                        request.getLanguageId()
                ))
                .build();
    }

    @Operation(
            summary = "Get all solution of a problem"
    )
    @GetMapping("/{problemId}/solutions")
    public ApiResponse<List<DetailsSolutionResponse>> getSolutionByProblemId(@PathVariable UUID problemId) {
        return ApiResponse.<List<DetailsSolutionResponse>>builder()
                .result(solutionService.getSolutionByProblemId(problemId))
                .build();
    }

    @Operation(
            summary = "(testing only) Get problem by id in file"
    )
    @GetMapping("/{problemId}/2")
    public ResponseEntity<Problem> getProblem2(@PathVariable UUID problemId) {
        try {
            Problem problem = null;
                    problemService.getProblemById(problemId);
            return ResponseEntity.ok(problem); // Return the problem if found
        } catch (AppException e) {
            if (e.getErrorCode() == ErrorCode.PROBLEM_NOT_EXIST) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // Return 404 if problem not found
            }
            throw e;
        }
    }

    @Operation(
            summary = "Get partial boilerplate of problem by id"
    )
    @GetMapping("/{problemId}/partial-boilerplate")
    public ApiResponse<List<PartialBoilerplateResponse>> getPartialBoilerplate(@PathVariable UUID problemId) {
        return ApiResponse.<List<PartialBoilerplateResponse>>builder()
                .result(problemService.getPartialBoilerplateOfProblem(problemId))
                .build();
    }
}
