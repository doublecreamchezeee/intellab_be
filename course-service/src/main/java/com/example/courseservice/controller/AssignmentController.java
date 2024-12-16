package com.example.courseservice.controller;


import com.example.courseservice.dto.ApiResponse;
import com.example.courseservice.dto.request.Assignment.AssignmentCreationRequest;
import com.example.courseservice.dto.request.Assignment.AssignmentDetailRequest;
import com.example.courseservice.dto.response.Assignment.AssignmentDetailResponse;
import com.example.courseservice.dto.response.Assignment.AssignmentResponse;
import com.example.courseservice.service.AssignmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/assignments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Assignment")
public class AssignmentController {
    AssignmentService assignmentService;

    @Operation(
            summary = "Get one assignments by id"
    )
    @GetMapping("/{assignmentId}")
    ApiResponse<AssignmentResponse> getAssignment(@PathVariable UUID assignmentId) {
        return ApiResponse.<AssignmentResponse>builder()
                .result(assignmentService.getAssignmentById(assignmentId))
                .build();
    }

    @Operation(
            summary = "Create assignment"
    )
    @PostMapping
    ApiResponse<AssignmentResponse> createAssignment(@RequestBody AssignmentCreationRequest request) {
        return ApiResponse.<AssignmentResponse>builder()
                .result(assignmentService.addAssignment(request))
                .build();
    }

    @Operation(
            summary = "Add details information (answer, unit score) to assignment by id"
    )
    @PostMapping("/{assignmentId}")
    ApiResponse<AssignmentResponse> addDetails(@PathVariable UUID assignmentId,@RequestBody List<AssignmentDetailRequest> requests) {
        return ApiResponse.<AssignmentResponse>builder()
                .result(assignmentService.addDetail(assignmentId,requests))
                .build();
    }

    @Operation(
            summary = "Get details information of assignment by id"
    )
    @GetMapping("/{assignmentId}/details")
    ApiResponse<List<AssignmentDetailResponse>> getDetails(@PathVariable UUID assignmentId) {
        return ApiResponse.<List<AssignmentDetailResponse>>builder()
                .result(assignmentService.getAssignmentDetail(assignmentId))
                .build();
    }
}
