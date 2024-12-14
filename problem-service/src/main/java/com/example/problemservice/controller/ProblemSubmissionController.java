package com.example.problemservice.controller;

import com.example.problemservice.model.ProblemSubmission;
import com.example.problemservice.service.ProblemSubmissionService;
import com.example.problemservice.exception.AppException;
import com.example.problemservice.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/problem-submissions")
@RequiredArgsConstructor
public class ProblemSubmissionController {
    private final ProblemSubmissionService problemSubmissionService;

    @PostMapping
    public ResponseEntity<ProblemSubmission> createSubmission(@RequestBody ProblemSubmission submission) {
        try {
            ProblemSubmission createdSubmission = problemSubmissionService.submitProblem(submission);
            return ResponseEntity.ok(createdSubmission); // HTTP 200 OK
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // HTTP 500
        }
    }

    @PostMapping("/update/{submissionId}")
    public ResponseEntity<ProblemSubmission> updateSubmission(@PathVariable UUID submissionId) {
        ProblemSubmission submission = problemSubmissionService.updateSubmissionResult(submissionId);
        return ResponseEntity.ok(submission);
    }

    @GetMapping("/{submissionId}")
    public ResponseEntity<ProblemSubmission> getSubmission(@PathVariable UUID submissionId) {
        try {
            ProblemSubmission submission = problemSubmissionService.getSubmission(submissionId);
            return ResponseEntity.ok(submission); // HTTP 200 OK
        } catch (AppException e) {
            if (e.getErrorCode() == ErrorCode.SUBMISSION_NOT_EXIST) {
                return ResponseEntity.status(ErrorCode.SUBMISSION_NOT_EXIST.getStatusCode())
                        .body(null); // HTTP 400 BAD REQUEST
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // HTTP 500
        }
    }
}
