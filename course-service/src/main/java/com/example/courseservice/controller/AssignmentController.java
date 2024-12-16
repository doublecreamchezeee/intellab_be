package com.example.courseservice.controller;


import com.example.courseservice.dto.ApiResponse;
import com.example.courseservice.dto.request.Assignment.AssignmentCreationRequest;
import com.example.courseservice.dto.request.Assignment.AssignmentDetailRequest;
import com.example.courseservice.dto.response.Assignment.AssignmentDetailResponse;
import com.example.courseservice.dto.response.Assignment.AssignmentResponse;
import com.example.courseservice.service.AssignmentService;
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
public class AssignmentController {
    AssignmentService assignmentService;

    @GetMapping("/{assignmentId}")
    ApiResponse<AssignmentResponse> getAssignment(@PathVariable UUID assignmentId) {
        return ApiResponse.<AssignmentResponse>builder()
                .result(assignmentService.getAssignmentById(assignmentId))
                .build();
    }

    @PostMapping
    ApiResponse<AssignmentResponse> createAssignment(@RequestBody AssignmentCreationRequest request) {
        return ApiResponse.<AssignmentResponse>builder()
                .result(assignmentService.addAssignment(request))
                .build();
    }

    @PostMapping("/{assignmentId}")
    ApiResponse<AssignmentResponse> addDetails(@PathVariable UUID assignmentId,@RequestBody List<AssignmentDetailRequest> requests) {
        return ApiResponse.<AssignmentResponse>builder()
                .result(assignmentService.addDetail(assignmentId,requests))
                .build();
    }

    @GetMapping("/{assignmentId}/details")
    ApiResponse<List<AssignmentDetailResponse>> getDetails(@PathVariable UUID assignmentId) {
        return ApiResponse.<List<AssignmentDetailResponse>>builder()
                .result(assignmentService.getAssignmentDetail(assignmentId))
                .build();
    }
}
