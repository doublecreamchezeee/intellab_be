package com.example.problemservice.controller;

import com.example.problemservice.dto.request.TestCaseCreationRequest;
import com.example.problemservice.dto.request.testcase.TestCaseMultipleCreationRequest;
import com.example.problemservice.dto.request.testcase.TestCasesGenerationRequest;
import com.example.problemservice.model.Problem;
import com.example.problemservice.model.TestCase;
import com.example.problemservice.service.TestCaseService;
import com.example.problemservice.utils.ParseUUID;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
    public ResponseEntity<TestCase> createTestCase(
            @RequestBody TestCaseCreationRequest request,
            @RequestHeader("X-UserId") String userUid
    ){
        userUid = userUid.split(",")[0];

        TestCase response = testCaseService.createTestCase(
                ParseUUID.normalizeUID(userUid),
                request
        );
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Create multiple test case"
    )
    @PostMapping("/multiple")
    public ResponseEntity<List<TestCase>> createMultipleProblem(
            @RequestBody TestCaseMultipleCreationRequest request,
            @RequestHeader("X-UserId") String userUid
    ){
        userUid = userUid.split(",")[0];

        List<TestCase> response = testCaseService.createMultipleTestCases(
                ParseUUID.normalizeUID(userUid),
                request
        );

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get supported data types"
    )
    @GetMapping("/supported-data-types")
    public ResponseEntity<List<String>> getSupportedDataTypes() {
        return ResponseEntity.ok(testCaseService.getSupportedDataTypes());
    }

    @Operation(
            summary = "Get all test cases"
    )
    @GetMapping
    public ResponseEntity<List<TestCase>> getTestCases() {
        List<TestCase> response = testCaseService.getAllTestCases();
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get test case by id"
    )
    @GetMapping("/{id}")
    public ResponseEntity<TestCase> getTestCasesById(@PathVariable String id) {
        TestCase response = testCaseService.getTestCase(UUID.fromString(id));
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get test cases by problem id"
    )
    @GetMapping("/problem/{problemId}")
    public ResponseEntity<List<TestCase>> getTestCasesByProblemId(@PathVariable String problemId) {
        List<TestCase> response = testCaseService.getTestCasesByProblemId(UUID.fromString(problemId));
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "(testing) Generate test cases"
    )
    @PostMapping("/generate")
    public <T extends Number> ResponseEntity<List<T[]>> generateTestCases(@RequestBody TestCasesGenerationRequest<T> request) {
        List<T[]> testCases = testCaseService.generateNumericTestCases(request);
        return ResponseEntity.ok(testCases);
    }
}
