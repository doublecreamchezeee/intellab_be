package com.example.problemservice.controller;

import com.example.problemservice.dto.request.problemRunCode.DetailsProblemRunCodeRequest;
import com.example.problemservice.dto.response.ApiResponse;
import com.example.problemservice.dto.response.SubmissionCallbackResponse;
import com.example.problemservice.dto.response.problemRunCode.CreationProblemRunCodeResponse;
import com.example.problemservice.dto.response.problemRunCode.DetailsProblemRunCodeResponse;
import com.example.problemservice.service.ProblemRunCodeService;
import com.example.problemservice.utils.ParseUUID;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/problem-run-code")
@RequiredArgsConstructor
@Tag(name = "Run Code")
public class ProblemRunCodeController {
    private final ProblemRunCodeService problemRunCodeService;

    @Operation(
            summary = "Create run code request"
    )
    @PostMapping
    public ApiResponse<CreationProblemRunCodeResponse> createRunCode(
            @RequestBody DetailsProblemRunCodeRequest request,
            @RequestHeader("X-UserId") String userUid,
            @RequestParam(value = "base64", required = false) Boolean base64
    ) {
        if (base64 == null) {
            base64 = false;
        }

        userUid = userUid.split(",")[0];
        CreationProblemRunCodeResponse response = problemRunCodeService.runCode(
                ParseUUID.normalizeUID(userUid),
                request,
                base64);

        return ApiResponse.<CreationProblemRunCodeResponse>builder()
                    .message("Submits to run code successfully")
                    .result(response)
                    .build();
    }

    @Operation(
            summary = "Create run code batch request"
    )
    @PostMapping("/batch")
    public ApiResponse<CreationProblemRunCodeResponse> createRunCodeBatch(
            @RequestBody DetailsProblemRunCodeRequest request,
            @RequestHeader("X-UserId") String userUid,
            @RequestParam(value = "base64", required = false) Boolean base64
    ) {
        if (base64 == null) {
            base64 = false;
        }

        userUid = userUid.split(",")[0];
        CreationProblemRunCodeResponse response = problemRunCodeService.runCodeBatch(
                ParseUUID.normalizeUID(userUid),
                request,
                base64);

        return ApiResponse.<CreationProblemRunCodeResponse>builder()
                .message("Submits to run code successfully")
                .result(response)
                .build();
    }

    @Operation(
            summary = "(BE only) Callback update run code by id",
            hidden = true
    )
    @PutMapping("/update/run-code/callback")
    public ApiResponse<Object> callbackUpdateRunCode(@RequestBody SubmissionCallbackResponse request) {
        //System.out.println("Callback update run code by id: " + request);
        problemRunCodeService.callbackUpdate(request);

        return ApiResponse.<Object>builder()
                .message("Callback update runcode successfully")
                .result(request)
                .build();
    }

    @Operation(
            summary = "Get run code by id"
    )
    @GetMapping("/{runCodeId}")
    public ApiResponse<DetailsProblemRunCodeResponse> getRunCodeById(@PathVariable UUID runCodeId) {
        DetailsProblemRunCodeResponse response = problemRunCodeService.getRunCodeById(runCodeId);

        return ApiResponse.<DetailsProblemRunCodeResponse>builder()
                .message("Get run code successfully")
                .result(response)
                .build();
    }

}
