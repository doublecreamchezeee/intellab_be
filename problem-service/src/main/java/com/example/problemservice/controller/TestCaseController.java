package com.example.problemservice.controller;

import com.example.problemservice.dto.request.TestCaseCreationRequest;
import com.example.problemservice.dto.request.testcase.TestCaseMultipleCreationRequest;
import com.example.problemservice.model.Problem;
import com.example.problemservice.model.TestCase;
import com.example.problemservice.service.TestCaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<TestCase> createProblem(@RequestBody TestCaseCreationRequest request) {
        TestCase response = testCaseService.createTestCase(request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Create multiple test case"
    )
    @PostMapping("/multiple")
    public ResponseEntity<List<TestCase>> createMultipleProblem(@RequestBody TestCaseMultipleCreationRequest request) {
        List<TestCase> response = testCaseService.createMultipleTestCases(request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get supported data types"
    )
    @GetMapping("/supported-data-types")
    public ResponseEntity<List<String>> getSupportedDataTypes() {
        return ResponseEntity.ok(testCaseService.getSupportedDataTypes());
    }

}
