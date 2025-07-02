package com.example.problemservice.controller;

import com.example.problemservice.client.MossClient;
import com.example.problemservice.dto.request.ProblemSubmission.DetailsProblemSubmissionRequest;
import com.example.problemservice.dto.request.ProblemSubmission.SubmitCodeRequest;
import com.example.problemservice.dto.response.ApiResponse;
import com.example.problemservice.dto.response.SubmissionCallbackResponse;
import com.example.problemservice.dto.response.problemSubmission.DetailsProblemSubmissionResponse;
import com.example.problemservice.dto.response.problemSubmission.MossMatchResponse;
import com.example.problemservice.dto.response.problemSubmission.ProblemSubmissionResponse;
import com.example.problemservice.model.ProblemSubmission;
import com.example.problemservice.repository.ProblemSubmissionRepository;
import com.example.problemservice.service.ProblemSubmissionService;
import com.example.problemservice.exception.AppException;
import com.example.problemservice.exception.ErrorCode;
import com.example.problemservice.utils.ParseUUID;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Logger;

@RestController
@RequestMapping("/problem-submissions")
@RequiredArgsConstructor
@Tag(name = "Submission")
public class ProblemSubmissionController {
    private final ProblemSubmissionService problemSubmissionService;
    private final MossClient mossClient;

    @Operation(
            summary = "Create submission (Submit code)"
    )
    @PostMapping
    public ResponseEntity<DetailsProblemSubmissionResponse> createSubmission(
            @RequestBody SubmitCodeRequest request,
            @RequestParam(value = "base64", required = false) Boolean base64,
            @RequestHeader("X-EmailVerified") Boolean emailVerified
    ) {
        if (emailVerified == null || !emailVerified) {
            throw new AppException(ErrorCode.EMAIL_NOT_VERIFIED);
        }

        if (base64 == null) {
            base64 = false;
        }

        try {
            DetailsProblemSubmissionResponse createdSubmission = problemSubmissionService.submitProblem(request, base64);
            return ResponseEntity.ok(createdSubmission); // HTTP 200 OK
        } catch (Exception e) {
            System.out.println(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // HTTP 500
        }
    }

    @Operation(
            summary = "Get submission by user id"
    )
    @GetMapping("/submitList/me")
    public ResponseEntity<Page<DetailsProblemSubmissionResponse>> getSubmissionByUserId(
            @RequestHeader(name = "X-UserId", required = false) String userUid,
            @RequestParam(required = false) String UserUid,
            @ParameterObject Pageable pageable) {

        if (userUid == null && UserUid == null) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

        if (UserUid != null) {
            Page<DetailsProblemSubmissionResponse> submission = problemSubmissionService
                    .getSubmissionDetailsByUserUid(
                            ParseUUID.normalizeUID(UserUid), pageable);

            return ResponseEntity.ok(submission);
        }

        userUid = userUid.split(",")[0];
        System.out.println(userUid);
        System.out.println(ParseUUID.normalizeUID(userUid));

        Page<DetailsProblemSubmissionResponse> submission = problemSubmissionService.getSubmissionDetailsByUserUid(ParseUUID.normalizeUID(userUid), pageable);

        return ResponseEntity.ok(submission);
    }

    @Operation(
            summary = "List submission by userid & problemId (Show the problem submission in submission tab)"
    )
    @GetMapping("/submitList/{problemId}")
    public ResponseEntity<Page<ProblemSubmissionResponse>> getSubmissions(@PathVariable String problemId, @RequestHeader("X-UserId") String userId, @ParameterObject Pageable pageable) {
        userId = userId.split(",")[0];

        System.out.println(userId);
        System.out.println(ParseUUID.normalizeUID(userId));
        Page<ProblemSubmissionResponse> submissions = problemSubmissionService.getSubmissionsByUserId(UUID.fromString(problemId), ParseUUID.normalizeUID(userId), pageable);
        return ResponseEntity.ok(submissions);
    }

    @Operation(
            summary = "(BE only) Callback update submission by id"
    )
    @PutMapping("/update/submission/callback")
    public ResponseEntity<Object> callbackUpdateSubmission(@RequestBody SubmissionCallbackResponse request) {
        System.out.println("Callback update submission by id: " + request);
        ProblemSubmission submission = problemSubmissionService.callbackUpdate(request);
        // Here you can implement further logic like saving to the database or processing the response

        return ResponseEntity.ok(request);
    }

    @Operation(
            summary = "Update submission by id (Unused api-call)",
            hidden = true
    )
    @PostMapping("/update/{submissionId}")
    public ResponseEntity<ProblemSubmission> updateSubmission(@PathVariable UUID submissionId) {
        ProblemSubmission submission = problemSubmissionService.updateSubmissionResult(submissionId);
        return ResponseEntity.ok(submission);
    }

    @Operation(
            summary = "Get submission by id, call after submit code to set the isSolved is true if accepted"
    )
    @GetMapping("/{submissionId}")
    public ResponseEntity<DetailsProblemSubmissionResponse> getSubmission(@PathVariable String submissionId) {
        try {
            DetailsProblemSubmissionResponse submission = problemSubmissionService.getSubmission(UUID.fromString(submissionId));
            return ResponseEntity.ok(submission); // HTTP 200 OK
        } catch (AppException e) {
            if (e.getErrorCode() == ErrorCode.SUBMISSION_NOT_EXIST) {
                return ResponseEntity.status(ErrorCode.SUBMISSION_NOT_EXIST.getStatusCode())
                        .body(null); // HTTP 400 BAD REQUEST
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // HTTP 500
        }
    }

    @Operation(
            summary = "Get submission details by problem id and user uid (to see all submission of user in a problem)"
    )
    @GetMapping("/details/{problemId}")
    public ApiResponse<Page<DetailsProblemSubmissionResponse>> getSubmissionDetailsByProblemIdAndUserUid(
            @PathVariable("problemId") UUID problemId,
            @RequestHeader("X-UserId") String userUid,
            @ParameterObject Pageable pageable
            //@PathVariable("userId") UUID userUid
    ) {
        userUid = userUid.split(",")[0];

        return ApiResponse.<Page<DetailsProblemSubmissionResponse>>builder()
                .result(problemSubmissionService.getSubmissionDetailsByProblemIdAndUserUid(
                                problemId,
                                ParseUUID.normalizeUID(userUid),
                                pageable
                        )
                )
                .message("Submission details retrieved successfully")
                .code(200)
                .build();

    }

    @Operation(
            summary = "Create submission with partial boilerplate"
    )
    @PostMapping("/partial-boilerplate")
    public ResponseEntity<ProblemSubmission> createSubmissionWithPartialBoilerplate(
            @RequestBody DetailsProblemSubmissionRequest submission,
            @RequestHeader("X-UserId") String userUid
    ) {
        try {
            userUid = userUid.split(",")[0];
            ProblemSubmission createdSubmission = problemSubmissionService.submitProblemWithPartialBoilerplate(
                    ParseUUID.normalizeUID(userUid),
                    submission);
            return ResponseEntity.ok(createdSubmission); // HTTP 200 OK
        } catch (Exception e) {
            System.out.println(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // HTTP 500
        }
    }

    @Getter
    @Setter
    public static class MossTestRequest{
        List<String> codeSnippets;
        String language;
        String baseCode;
    }

    @Operation(
            summary = "BE only"
    )
    @PostMapping("/check")
    public ResponseEntity<?> runMossTest(
            @RequestBody MossTestRequest request
    ) {
        try {
            String reportUrl = mossClient.runMoss(
                    request.getCodeSnippets(),
                    request.getLanguage(),
                    request.getBaseCode()
            );
            return ResponseEntity.ok().body(reportUrl);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed: " + e.getMessage());
        }
    }

    @Operation(
            summary = "BE only"
    )
    @PostMapping("/moss/{submissionId}")
    public ResponseEntity<List<MossMatchResponse>> runMoss(
            @PathVariable String submissionId
    ) throws IOException, InterruptedException {

        List<MossMatchResponse> responses =  problemSubmissionService.mossService(UUID.fromString(submissionId));
        return ResponseEntity.ok().body(responses);
    }
}
