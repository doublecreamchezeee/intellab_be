package com.example.problemservice.controller;

import com.example.problemservice.dto.request.TestCaseCreationRequest;
import com.example.problemservice.dto.request.testcase.TestCaseMultipleCreationRequest;
import com.example.problemservice.dto.request.testcase.TestCasesGenerationRequest;
import com.example.problemservice.dto.response.ApiResponse;
import com.example.problemservice.dto.response.testcase.TestCaseCreationResponse;
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
  final String defaultRole = "myRole";

  private Boolean isAdmin(String role) {
    return role.contains("admin");
  }

  @Operation(summary = "Create test case")
  @PostMapping("/admin/create-testcase")
  public ApiResponse<TestCaseCreationResponse> createTestCase(
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

  @Operation(summary = "Create multiple test case")
  @PostMapping("/multiple")
  public ApiResponse<List<TestCaseCreationResponse>> createMultipleProblem(
      @RequestBody TestCaseMultipleCreationRequest request,

      @RequestHeader("X-UserId") String userUid) {

    List<TestCaseCreationResponse> response = testCaseService.createMultipleTestCases(
        request);

    return ApiResponse.<List<TestCaseCreationResponse>>builder()
            .result(response)
            .code(200)
            .message("Created")
            .build();
  }

  @Operation(summary = "Get supported data types")
  @GetMapping("/supported-data-types")
  public ResponseEntity<List<String>> getSupportedDataTypes() {
    return ResponseEntity.ok(testCaseService.getSupportedDataTypes());
  }

  @Operation(summary = "Get all test cases")
  @GetMapping
  public ResponseEntity<List<TestCase>> getTestCases() {
    List<TestCase> response = testCaseService.getAllTestCases();
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Get test case by id")
  @GetMapping("/{id}")
  public ResponseEntity<TestCase> getTestCasesById(@PathVariable String id) {
    TestCase response = testCaseService.getTestCase(UUID.fromString(id));
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Get test cases by problem id")
  @GetMapping("/problem/{problemId}")
  public ResponseEntity<List<TestCase>> getTestCasesByProblemId(@PathVariable String problemId) {
    List<TestCase> response = testCaseService.getTestCasesByProblemId(UUID.fromString(problemId));
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "(testing) Generate test cases")
  @PostMapping("/generate")
  public <T extends Number> ResponseEntity<List<T[]>> generateTestCases(
      @RequestBody TestCasesGenerationRequest<T> request) {
    List<T[]> testCases = testCaseService.generateNumericTestCases(request);
    return ResponseEntity.ok(testCases);
  }
}
