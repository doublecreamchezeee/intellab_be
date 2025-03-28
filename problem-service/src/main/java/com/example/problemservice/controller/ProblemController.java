package com.example.problemservice.controller;

import com.example.problemservice.core.DoublePageable;
import com.example.problemservice.client.CourseClient;
import com.example.problemservice.dto.request.DefaultCodeRequest;
import com.example.problemservice.dto.request.problem.EnrichCodeRequest;
import com.example.problemservice.dto.request.problem.ProblemCreationRequest;
import com.example.problemservice.dto.response.ApiResponse;
import com.example.problemservice.dto.response.DefaultCode.DefaultCodeResponse;
import com.example.problemservice.dto.response.DefaultCode.PartialBoilerplateResponse;
import com.example.problemservice.dto.response.Problem.DetailsProblemResponse;
import com.example.problemservice.dto.response.Problem.ProblemCreationResponse;
import com.example.problemservice.dto.response.Problem.ProblemRowResponse;
import com.example.problemservice.dto.response.problemComment.DetailsProblemCommentResponse;
import com.example.problemservice.dto.response.solution.DetailsSolutionResponse;
import com.example.problemservice.model.Problem;
import com.example.problemservice.model.ProblemComment;
import com.example.problemservice.service.ProblemCommentService;
import com.example.problemservice.model.course.Category;
import com.example.problemservice.service.ProblemService;
import com.example.problemservice.exception.AppException;
import com.example.problemservice.exception.ErrorCode;
import com.example.problemservice.utils.ParseUUID;
import com.example.problemservice.service.SolutionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
@Slf4j
public class ProblemController {
    private final ProblemService problemService;
    private final SolutionService solutionService;
    private final ProblemCommentService problemCommentService;
    final String defaultRole = "myRole";

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
    public ResponseEntity<DetailsProblemResponse> getProblem(
            @PathVariable UUID problemId,
            @RequestHeader(value = "X-UserId", required = false) String userId,
            @RequestHeader(value = "X-UserRole", required = false) String role
    ) {

        if (role == null || role.equals(defaultRole)) {
            role = "user,free";
        }

        //log.info("role: {}", (Object) role.split(","));
        String subscriptionPlan = role.split(",")[1];

        UUID userUuid = null;

        if (userId != null) {
            String userUid = userId.split(",")[0];
            userUuid = ParseUUID.normalizeUID(userUid);
        }

        try {
            DetailsProblemResponse problem = problemService.getProblem(
                    problemId,
                    subscriptionPlan,
                    userUuid
            );
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
    public ApiResponse<Page<ProblemRowResponse>> getProblems(@RequestParam(required = false) List<Integer> categories,
                                                             @RequestParam(required = false) String level,
                                                             @RequestParam(required = false) Boolean status,
                                                             @RequestHeader(required = false, name = "X-UserId") String userUId,
                                                             @ParameterObject Pageable pageable,
                                                             @RequestParam(required = false) String keyword) {
        if (userUId != null) {
            UUID userId = ParseUUID.normalizeUID(userUId);
            System.out.println("user id: " + userId);
            if(keyword != null) {
                return ApiResponse.<Page<ProblemRowResponse>>builder()
                        .result(problemService.searchProblems(categories, level, status, pageable, keyword, userId)).build();
            }

            return ApiResponse.<Page<ProblemRowResponse>>builder()
                    .result(problemService.getAllProblems(categories, level, status, pageable, userId)).build();
        }
        if(keyword != null) {
            return ApiResponse.<Page<ProblemRowResponse>>builder()
                    .result(problemService.searchProblems(categories, level, pageable,keyword)).build();
        }
        return ApiResponse.<Page<ProblemRowResponse>>builder()
                .result(problemService.getAllProblems(categories, level, pageable)).build();
    }

    @Operation(
            summary = "Get all problems"
    )
    @GetMapping("")
    public ApiResponse<Page<ProblemRowResponse>> getAllProblem() {
        Pageable pageable = PageRequest.of(0, 1000);
        try {
            //Page<ProblemRowResponse> response = problemService.getAllProblems(pageable);
            return ApiResponse.<Page<ProblemRowResponse>>builder()
                    .result(problemService.getAllProblems(pageable)).build();
        } catch (AppException e) {
            if (e.getErrorCode() == ErrorCode.PROBLEM_NOT_EXIST) {
                ApiResponse.<Page<ProblemRowResponse>>builder()
                        .result(null)
                        .code(ErrorCode.PROBLEM_NOT_EXIST.getCode())
                        .message(String.valueOf(ErrorCode.PROBLEM_NOT_EXIST))
                        .build();
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
            summary = "(testing only) Generate boilerplate code"
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
            //,hidden = true
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

    @Operation(
            summary = "Get all problem comment by problem id"
    )
    @GetMapping("/{problemId}/comments")
    public ApiResponse<Page<DetailsProblemCommentResponse>> getAllProblemCommentByProblemId(
            @PathVariable UUID problemId,
            @ParameterObject Pageable pageable,
            @RequestParam(name = "childrenPage", required = false, defaultValue = "0") Integer childrenPage,
            @RequestParam(name = "childrenSize", required = false, defaultValue = "20") Integer childrenSize,
            @RequestParam(defaultValue = "lastModifiedAt", required = false) String childrenSortBy,
            @RequestParam(defaultValue = "asc", required = false) String childrenSortOrder,
            @RequestHeader(required = false, name = "X-UserId") String userUid
        /*     @Qualifier("pageable")  @ParameterObject DoublePageable doublePageable
            @Qualifier("childrenPageable") @ParameterObject Pageable childrenPageable*/
    ) {
        //log.info("userUid: {}", userUid);
        if (childrenPage == null) {
            childrenPage = 0;
        }

        if (childrenSize == null) {
            childrenSize = 20;
        }

        Sort sort = childrenSortOrder.equalsIgnoreCase("desc")
                        ? Sort.by(childrenSortBy).descending()
                        : Sort.by(childrenSortBy).ascending();

        Pageable childrenPageable = PageRequest.of(childrenPage, childrenSize, sort);
                //Sort.by("lastModifiedAt").ascending());

        //Pageable.ofSize(childrenSize).withPage(childrenPage);

        UUID userUuid = null;

        if (userUid != null) {
            userUid = userUid.split(",")[0];
            userUuid = ParseUUID.normalizeUID(
                    userUid
            );
        }

        return ApiResponse.<Page<DetailsProblemCommentResponse>>builder()
                .result(
                        problemCommentService.getAllProblemCommentByProblemId(
                                problemId,
                                userUuid,
                                pageable,
                                childrenPageable
                        )
                )
                .build();
    }
}
