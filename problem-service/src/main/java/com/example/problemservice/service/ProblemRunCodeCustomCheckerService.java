package com.example.problemservice.service;

import com.example.problemservice.client.BoilerplateClient;
import com.example.problemservice.client.CourseClient;
import com.example.problemservice.client.CustomCheckerJudge0Client;
import com.example.problemservice.dto.request.problemRunCode.DetailsProblemRunCodeRequest;
import com.example.problemservice.dto.response.SubmissionCallbackResponse;
import com.example.problemservice.dto.response.problemRunCode.CreationProblemRunCodeResponse;
import com.example.problemservice.dto.response.problemRunCode.DetailsProblemRunCodeResponse;
import com.example.problemservice.exception.AppException;
import com.example.problemservice.exception.ErrorCode;
import com.example.problemservice.mapper.ProblemRunCodeMapper;
import com.example.problemservice.mapper.TestCaseRunCodeOutputMapper;
import com.example.problemservice.model.*;
import com.example.problemservice.model.composite.TestCaseRunCodeOutputId;
import com.example.problemservice.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class ProblemRunCodeCustomCheckerService {
    ProblemRepository problemRepository;
    TestCaseOutputRepository testCaseOutputRepository;
    CustomCheckerJudge0Client customCheckerJudge0Client;
    BoilerplateClient boilerplateClient;
    ProgrammingLanguageRepository programmingLanguageRepository;
    TestCaseRepository testCaseRepository;
    CourseClient courseClient;
    NotificationService notificationService;
    ProblemRunCodeMapper problemRunCodeMapper;
    ProblemRunCodeRepository problemRunCodeRepository;
    TestCaseRunCodeOutputRepository testCaseRunCodeOutputRepository;
    TestCaseRunCodeOutputMapper testCaseRunCodeOutputMapper;

    public Hashtable<Integer, Boolean> getHashTableFromProblemCategories(List<ProblemCategory> problemCategories) {
        Hashtable<Integer, Boolean> categories = new Hashtable<>();
        if (problemCategories != null) {
            for (ProblemCategory problemCategory : problemCategories) {
                categories.put(problemCategory.getProblemCategoryID().getCategoryId(), true);
            }
        }
        return categories;
    }

    public CreationProblemRunCodeResponse runCodeBatch(UUID userId, DetailsProblemRunCodeRequest request, Boolean base64) {

        // Lấy Problem
        Problem problem = problemRepository.findById(request.getProblemId()).orElseThrow(
                () -> new AppException(ErrorCode.PROBLEM_NOT_EXIST)
        );

        ProgrammingLanguage language = programmingLanguageRepository.findById(request.getLanguageId())
                .orElseThrow(() -> new AppException(ErrorCode.PROGRAMMING_LANGUAGE_NOT_EXIST));

        //DELETE ALL PREVIOUS RUN CODES
        List<ProblemRunCode> runCodes = problemRunCodeRepository.findProblemRunCodeByUserIdAndProblem_ProblemId(
                userId,
                request.getProblemId()
        );

        runCodes.forEach(runCode -> {
            testCaseRunCodeOutputRepository.deleteAll(runCode.getTestCasesRunCodeOutput());
        });
        problemRunCodeRepository.deleteAll(runCodes);

        ProblemRunCode problemRunCode = problemRunCodeMapper.toProblemRunCode(request);

        problemRunCode.setUserId(userId);
        problemRunCode.setProblem(problem);

        if (base64!=null && base64) {
            Base64.Decoder decoder = Base64.getDecoder();
            problemRunCode.setCode(
                    new String(
                            decoder.decode(
                                    problemRunCode.getCode()
                            )
                    )
            );
        }

        problemRunCode.setCode(
                boilerplateClient.enrich(
                        problemRunCode.getCode(),
                        request.getLanguageId(),
                        problem.getProblemStructure(),
                        problem.getHasCustomChecker(),
                        problem.getAdditionalCheckerFields(),
                        getHashTableFromProblemCategories(problem.getCategories())

                )
        );

        log.info("Enriched code: {}", problemRunCode.getCode());

        problemRunCode.setProgrammingLanguage(language.getLongName());

        problemRunCode = problemRunCodeRepository.save(problemRunCode);

        List<TestCase> testCases = testCaseRepository.findAllByProblem_ProblemId(
                request.getProblemId()
        );

        //TODO: UNCOMMENT THIS LINE TO LIMIT THE NUMBER OF TEST CASES
        // Limit the number of test cases to NUMBER_OF_TEST_CASE
        //testCases = testCases.subList(0, Math.min(3, NUMBER_OF_TEST_CASE));

        List<TestCaseRunCodeOutput> outputs = customCheckerJudge0Client.runCodeBatchToGetActualOutput(
                problemRunCode,
                testCases
        );

        for (int i = 0; i < outputs.size(); i++) {
            TestCaseRunCodeOutput output = outputs.get(i);
            // Khởi tạo composite ID
            TestCaseRunCodeOutputId outputId = new TestCaseRunCodeOutputId();
            outputId.setRunCodeId(problemRunCode.getRunCodeId());
            outputId.setTestcaseId(testCases.get(i).getTestcaseId());

            // Gán composite ID và liên kết với ProblemRunCode
            output.setTestCaseRunCodeOutputID(outputId);
            output.setTestcase(testCases.get(i));
            output.setRunCode(problemRunCode);
            output.setHasCustomChecker(true);
            output.setIsPassedByCheckingCustomChecker(null);

            output = testCaseRunCodeOutputRepository.save(output);

            outputs.set(i, output);
        }

        problemRunCode.setTestCasesRunCodeOutput(outputs);

        problemRunCode = problemRunCodeRepository.save(problemRunCode);

        return problemRunCodeMapper.toCreationProblemRunCodeResponse(problemRunCode);
    }

    public void callbackUpdateRunCodeToGetActualOutput(SubmissionCallbackResponse request) {
        log.info("Received callback for run code with token: {}", request.getToken());

        TestCaseRunCodeOutput output = testCaseRunCodeOutputRepository.findByToken(
                UUID.fromString(
                        request.getToken()
                )
        ).orElseThrow(
                () -> new AppException(ErrorCode.TEST_CASE_RUN_CODE_OUTPUT_NOT_EXIST)
        );

        if (request.getTime()!=null) {
            output.setRuntime(Float.valueOf(request.getTime()));
        } else {
            output.setRuntime(null);
        }

        if (request.getStdout()!= null) {

            output.setSubmissionOutput(
                    decodeBase64(
                            request.getStdout()
                    ).replace(
                            "\n",
                            ""
                    ) // remove newline character
            );

        } else {
            output.setSubmissionOutput(null);
        }

        output.setResultStatus(
                "1st: " +
                request.getStatus()
                        .getDescription()
        );

        output.setStatusId(
                null
        ); //request.getStatus().getId()


        if (request.getStderr() != null) {
            output.setError(
                    decodeBase64(
                            request.getStderr()
                    )
            );
        } else {
            output.setError(null);
        }

        if (request.getMessage()!=null) {
            output.setMessage(
                    decodeBase64(
                            request.getMessage()
                    )
            );
        } else {
            output.setMessage(null);
        }

        output.setMemoryUsage(
                String.valueOf(request.getMemory())
        );

        if (request.getCompile_output() != null)
        {
            output.setCompileOutput(
                    decodeBase64(
                            request.getCompile_output()
                    )
            );
        } else {
            output.setCompileOutput(null);
        }

        TestCaseRunCodeOutput savedOutput = testCaseRunCodeOutputRepository.save(output);

        if (savedOutput.getError() == null && request.getStatus().getId() == 3) {
            log.info("Run code output is accepted, proceeding to check with custom checker for test case: {}", savedOutput.getTestcase().getTestcaseId());
            runCodeToCheckOneTestCaseByCustomChecker(
                    savedOutput,
                    output.getTestcase()
            );
        }

    }

    private String decodeBase64(String input) {
        if (input == null || input.isEmpty()) {
            return null;
        }
        Base64.Decoder decoder = Base64.getDecoder();
        return new String(decoder.decode(input.trim().replaceAll("\\s+", "")));
    }

    public void callbackUpdateRunCodeToCheckingResult(SubmissionCallbackResponse request) {
        log.info("Received callback for checking result with token: {}", request.getToken());

        TestCaseRunCodeOutput output = testCaseRunCodeOutputRepository.findByToken(
                UUID.fromString(
                        request.getToken()
                )
        ).orElseThrow(
                () -> new AppException(ErrorCode.TEST_CASE_RUN_CODE_OUTPUT_NOT_EXIST)
        );

        if (request.getStatus() == null) {
            throw new AppException(ErrorCode.STATUS_ID_NOT_FOUND);
        }

        output.setStatusId(
                request.getStatus().getId()
        );

        output.setResultStatus(
                output.getResultStatus() +
                " - 2nd: " +
                request.getStatus().getDescription()
        );

        if (request.getCompile_output() != null)
        {
            output.setCompileOutput(
                    decodeBase64(
                            request.getCompile_output()
                    )
            );
        } else {
            output.setCompileOutput(null);
        }

        if (request.getStderr() != null) {
            output.setError(
                    decodeBase64(
                            request.getStderr()
                    )
            );
        } else {
            output.setError(null);
        }

        output.setIsPassedByCheckingCustomChecker(
                request.getStatus().getId() == 3 // 3 is the ID for "Accepted"
        );

        log.info("Checking result for test case: {}, isPassed: {}, output: {}",
                output.getTestcase().getTestcaseId(),
                output.getIsPassedByCheckingCustomChecker(),
                request.getStdout()
        );

        testCaseRunCodeOutputRepository.save(output);
    }

    @Async
    public void runCodeToCheckOneTestCaseByCustomChecker(
            TestCaseRunCodeOutput testCaseRunCodeOutput,
            TestCase testCase
    ) {

        if (!testCaseRunCodeOutput.getHasCustomChecker()) {
            throw new AppException(ErrorCode.CUSTOM_CHECKER_NOT_ENABLED_FOR_PROBLEM);
        }

        Problem problem = problemRepository.findById(
                testCase.getProblem().getProblemId()
        ).orElseThrow(
                () -> new AppException(ErrorCode.PROBLEM_NOT_EXIST)
        );

        customCheckerJudge0Client.runCodeToCheckOneTestCaseByCustomChecker(
                testCaseRunCodeOutput,
                testCase,
                problem
        );

    }

    public DetailsProblemRunCodeResponse getRunCodeById(UUID runCodeId) {
        ProblemRunCode runCode = problemRunCodeRepository.findById(runCodeId)
                .orElseThrow(() -> new AppException(
                        ErrorCode.RUN_CODE_NOT_EXISTED)
                );

        DetailsProblemRunCodeResponse response = problemRunCodeMapper.toDetailsProblemRunCodeResponse(runCode);

        response.setTestcases(new ArrayList<>());

        runCode.getTestCasesRunCodeOutput().forEach(testCaseRunCodeOutput -> {
            response.getTestcases().add(
                    testCaseRunCodeOutputMapper.toDetailsTestCaseRunCodeOutput(testCaseRunCodeOutput)
            );
        });

        return response;
    }
}
