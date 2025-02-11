package com.example.problemservice.controller;

import com.example.problemservice.dto.response.SubmissionCallbackResponse;
import com.example.problemservice.dto.response.problemSubmission.DetailsProblemSubmissionResponse;
import com.example.problemservice.dto.response.problemSubmission.ProblemSubmissionResponse;
import com.example.problemservice.model.ProblemSubmission;
import com.example.problemservice.repository.ProblemSubmissionRepository;
import com.example.problemservice.service.ProblemSubmissionService;
import com.example.problemservice.exception.AppException;
import com.example.problemservice.exception.ErrorCode;
import com.example.problemservice.utils.ParseUUID;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    private final ProblemSubmissionRepository problemSubmissionRepository;

    @Operation(
            summary = "Create submission"
    )
    @PostMapping
    public ResponseEntity<ProblemSubmission> createSubmission(@RequestBody ProblemSubmission submission) {
        try {
            ProblemSubmission createdSubmission = problemSubmissionService.submitProblem(submission);
            return ResponseEntity.ok(createdSubmission); // HTTP 200 OK
        } catch (Exception e) {
            System.out.println(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // HTTP 500
        }
    }

    @GetMapping("/{problemId}/{userId}")
    public ResponseEntity<List<ProblemSubmissionResponse>> getSubmissions(@PathVariable String problemId, @PathVariable String userId) {
        List<ProblemSubmissionResponse> submissions = problemSubmissionService.getSubmissionsByUserId(UUID.fromString(problemId), UUID.fromString(userId));
        return ResponseEntity.ok(submissions);
    }

    @Operation(
            summary = "Callback update submission by id"
    )
    @PutMapping("/update/submission/callback")
    public ResponseEntity<Object> callbackUpdateSubmission(@RequestBody SubmissionCallbackResponse request) {
        System.out.println("Callback update submission by id: " + request);
        ProblemSubmission submission = problemSubmissionService.callbackUpdate(request);
        // Here you can implement further logic like saving to the database or processing the response

        return ResponseEntity.ok(request);
    }

    @Operation(
            summary = "Update submission by id"
    )
    @PostMapping("/update/{submissionId}")
    public ResponseEntity<ProblemSubmission> updateSubmission(@PathVariable UUID submissionId) {
        ProblemSubmission submission = problemSubmissionService.updateSubmissionResult(submissionId);
        return ResponseEntity.ok(submission);
    }

    @Operation(
            summary = "Get submission by id"
    )
    @GetMapping("/{submissionId}")
    public ResponseEntity<ProblemSubmission> getSubmission(@PathVariable String submissionId) {
        try {
            ProblemSubmission submission = problemSubmissionService.getSubmission(UUID.fromString(submissionId));
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
            summary = "Get submission details by problem id and user uid"
    )
    @GetMapping("/details/{problemId}/{userId}")
    public List<DetailsProblemSubmissionResponse> getSubmissionDetailsByProblemIdAndUserUid(
            @PathVariable("problemId") UUID problemId,
            @PathVariable("userId") UUID userUid) {
        return problemSubmissionService.getSubmissionDetailsByProblemIdAndUserUid(
                problemId,
                userUid
                /*UUID.fromString(problemId),
                UUID.fromString(userId)*/
        );
    }

}
