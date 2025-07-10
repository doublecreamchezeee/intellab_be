package com.example.problemservice.controller;

import com.example.problemservice.dto.request.problemRunCode.DetailsProblemRunCodeRequest;
import com.example.problemservice.dto.response.ApiResponse;
import com.example.problemservice.dto.response.SubmissionCallbackResponse;
import com.example.problemservice.dto.response.problemRunCode.CreationProblemRunCodeResponse;
import com.example.problemservice.dto.response.problemRunCode.DetailsProblemRunCodeResponse;
import com.example.problemservice.exception.AppException;
import com.example.problemservice.exception.ErrorCode;
import com.example.problemservice.service.ProblemRunCodeCustomCheckerService;
import com.example.problemservice.utils.ParseUUID;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/problem-run-code/custom-checker")
@RequiredArgsConstructor
@Tag(name = "Run Code With Custom Checker")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProblemRunCodeCustomCheckerController {
    ProblemRunCodeCustomCheckerService problemRunCodeCustomCheckerService;

    @Operation(
            summary = "Create run code batch with custom checker"
    )
    @PostMapping("")
    public ApiResponse<CreationProblemRunCodeResponse> createRunCodeBatchWithCustomChecker(
            @RequestBody DetailsProblemRunCodeRequest request,
            @RequestHeader("X-UserId") String userUid,
            @RequestParam(value = "base64", required = false) Boolean base64,
            @RequestHeader("X-EmailVerified") Boolean emailVerified
    ) {
        if (emailVerified == null || !emailVerified) {
            throw new AppException(ErrorCode.EMAIL_NOT_VERIFIED);
        }

        if (base64 == null) {
            base64 = false;
        }

        userUid = userUid.split(",")[0];

        CreationProblemRunCodeResponse response = problemRunCodeCustomCheckerService.runCodeBatch(
                ParseUUID.normalizeUID(userUid),
                request,
                base64
        );

        return ApiResponse.<CreationProblemRunCodeResponse>builder()
                .message("Submits to run code with custom checker successfully")
                .result(response)
                .build();
    }

    @Operation(
            summary = "(BE only) callback update run code to get actual output by id"
    )
    @PutMapping("/update/run-code/callback-to-get-actual-output")
    public ApiResponse<Object> callbackUpdateRunCodeToGetActualOutputById(
            @RequestBody SubmissionCallbackResponse request
    ) {
        problemRunCodeCustomCheckerService.callbackUpdateRunCodeToGetActualOutput(
                request
        );

        return ApiResponse.<Object>builder()
                .message("Callback update run code successfully")
                .result(null)
                .build();
    }

    @Operation(
            summary = "(BE only) callback update run code to checking result"
    )
    @PutMapping("/update/run-code/callback-to-checking-result")
    public ApiResponse<Object> callbackUpdateRunCodeToCheckingResult(
            @RequestBody SubmissionCallbackResponse request
    ) {
        problemRunCodeCustomCheckerService.callbackUpdateRunCodeToCheckingResult(
                request
        );

        return ApiResponse.<Object>builder()
                .message("Callback update run code to checking result successfully")
                .result(null)
                .build();
    }

    @Operation(
            summary = "Get run code by id"
    )
    @GetMapping("/{runCodeId}")
    public ApiResponse<DetailsProblemRunCodeResponse> getRunCodeById(
            @PathVariable("runCodeId") UUID runCodeId
    ) {
        DetailsProblemRunCodeResponse response = problemRunCodeCustomCheckerService.getRunCodeById(
                runCodeId
        );

        return ApiResponse.<DetailsProblemRunCodeResponse>builder()
                .message("Get run code successfully")
                .result(response)
                .build();
    }
}
