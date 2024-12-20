package com.example.problemservice.controller;

import com.example.problemservice.model.Problem;
import com.example.problemservice.service.ProblemService;
import com.example.problemservice.exception.AppException;
import com.example.problemservice.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/problems")
@RequiredArgsConstructor
@Tag(name = "Problem")
public class ProblemController {
    private final ProblemService problemService;

    @Operation(
            summary = "Create problem"
    )
    @PostMapping
    public ResponseEntity<Problem> createProblem(@RequestBody Problem problem) {
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

    @Operation(
            summary = "Delete problem"
    )
    @DeleteMapping("/{problemId}")
    public ResponseEntity<Void> deleteProblem(@PathVariable UUID problemId) {
        problemService.deleteProblem(problemId);
        return ResponseEntity.noContent().build();
    }
}
