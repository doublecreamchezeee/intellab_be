package com.example.problemservice.controller;

import com.example.problemservice.dto.request.TestCaseCreationRequest;
import com.example.problemservice.model.Problem;
import com.example.problemservice.model.TestCase;
import com.example.problemservice.service.TestCaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/test-case")
@RequiredArgsConstructor
@Tag(name = "TestCase")
public class TestCaseController {

    private final TestCaseService testCaseService;

    @Operation(
            summary = "Create test case"
    )
    @PostMapping
    public ResponseEntity<TestCase> createTestCase(@RequestBody TestCaseCreationRequest request) {
        TestCase response = testCaseService.createTestCase(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<TestCase>> getTestCases() {
        List<TestCase> response = testCaseService.getAllTestCases();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TestCase> getTestCasesById(@PathVariable String id) {
        TestCase response = testCaseService.getTestCase(UUID.fromString(id));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/problem/{problemId}")
    public ResponseEntity<List<TestCase>> getTestCasesByProblemId(@PathVariable String problemId) {
        List<TestCase> response = testCaseService.getTestCasesByProblemId(UUID.fromString(problemId));
        return ResponseEntity.ok(response);
    }
}
