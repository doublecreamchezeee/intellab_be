package com.example.problemservice.controller;

import com.example.problemservice.dto.request.problemRunCode.DetailsProblemRunCodeRequest;
import com.example.problemservice.dto.response.ApiResponse;
import com.example.problemservice.dto.response.SubmissionCallbackResponse;
import com.example.problemservice.dto.response.problemRunCode.CreationProblemRunCodeResponse;
import com.example.problemservice.dto.response.problemRunCode.DetailsProblemRunCodeResponse;
import com.example.problemservice.exception.AppException;
import com.example.problemservice.exception.ErrorCode;
import com.example.problemservice.service.ProblemRunCodeService;
import com.example.problemservice.utils.ParseUUID;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/problem-run-code")
@RequiredArgsConstructor
@Tag(name = "Run Code")
public class ProblemRunCodeController {
    private static final Logger log = LoggerFactory.getLogger(ProblemRunCodeController.class);
    private final ProblemRunCodeService problemRunCodeService;

    @Operation(
            summary = "Create run code request"
    )
    @PostMapping("/old-version")
    public ApiResponse<CreationProblemRunCodeResponse> createRunCode(
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
    @PostMapping("")
    public ApiResponse<CreationProblemRunCodeResponse> createRunCodeBatch(
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
    public ApiResponse<Object> callbackUpdateRunCode(
            @RequestBody SubmissionCallbackResponse request,
            @RequestParam(name = "has-custom-checker", required = false) Boolean hasCustomChecker
    ) {
        //System.out.println("Callback update run code by id: " + request);
        log.info("hasCustomChecker in callback: {}", hasCustomChecker);

        problemRunCodeService.callbackUpdate(request, hasCustomChecker);

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
