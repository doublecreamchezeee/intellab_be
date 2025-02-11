package com.example.problemservice.service;

import com.example.problemservice.client.BoilerplateClient;
import com.example.problemservice.client.CourseClient;
import com.example.problemservice.client.Judge0Client;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProblemRunCodeService {
    private static final Logger log = LoggerFactory.getLogger(ProblemSubmissionService.class);
    private final ProblemRepository problemRepository;
    private final Judge0Client judge0Client;
    private final BoilerplateClient boilerplateClient;
    private final ProgrammingLanguageRepository programmingLanguageRepository;
    private final TestCaseRepository testCaseRepository;
    private final ProblemRunCodeMapper problemRunCodeMapper;
    private final ProblemRunCodeRepository problemRunCodeRepository;
    private final TestCaseRunCodeOutputRepository testCaseRunCodeOutputRepository;
    private final TestCaseRunCodeOutputMapper testCaseRunCodeOutputMapper;
    private final int NUMBER_OF_TEST_CASE = 3;

    public CreationProblemRunCodeResponse runCode(UUID userId, DetailsProblemRunCodeRequest request) {
        // Get problem by problemId
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

        problemRunCode.setCode(
                boilerplateClient.enrich(
                        problemRunCode.getCode(),
                        request.getLanguageId(),
                        problem.getProblemStructure()
                )
        );

        problemRunCode.setProgrammingLanguage(language.getLongName());

        problemRunCode = problemRunCodeRepository.save(problemRunCode);

        List<TestCase> testCases = testCaseRepository.findAllByProblem_ProblemId(
                request.getProblemId()
        );

        List<TestCaseRunCodeOutput> outputs = new ArrayList<>();

        for (int i = 0; i < testCases.size() && i < NUMBER_OF_TEST_CASE; i++) {
            TestCase testCase = testCases.get(i);

            TestCaseRunCodeOutput output = judge0Client.runCode(
                    problemRunCode,
                    testCase
            );

            // Khởi tạo composite ID
            TestCaseRunCodeOutputId outputId = new TestCaseRunCodeOutputId();
            outputId.setRunCodeId(problemRunCode.getRunCodeId());
            outputId.setTestcaseId(testCase.getTestcaseId());

            // Gán composite ID và liên kết với ProblemRunCode
            output.setTestCaseRunCodeOutputID(outputId);
            output.setTestcase(testCase);
            output.setRunCode(problemRunCode);

            output = testCaseRunCodeOutputRepository.save(output);

            outputs.add(output);
        }

        problemRunCode.setTestCasesRunCodeOutput(outputs);

        problemRunCode = problemRunCodeRepository.save(problemRunCode);

        return problemRunCodeMapper.toCreationProblemRunCodeResponse(problemRunCode);
    }

    public CreationProblemRunCodeResponse callbackUpdate(SubmissionCallbackResponse request) {
        TestCaseRunCodeOutput output = testCaseRunCodeOutputRepository.findByToken(
                UUID.fromString(
                        request.getToken()
                )
        ).orElseThrow(
                () -> new AppException(
                        ErrorCode.TEST_CASE_RUN_CODE_OUTPUT_NOT_EXIST
                )
        );

        if (request.getTime()!=null) {
            output.setRuntime(Float.valueOf(request.getTime()));
        } else {
            output.setRuntime(null);
        }

        Base64.Decoder decoder = Base64.getDecoder();

        if (request.getStdout()!= null) {

            output.setSubmissionOutput(
                    new String(
                            decoder.decode(
                                    request.getStdout()
                                            .trim()
                                            .replaceAll(
                                                "\\s+",
                                                ""
                                            )
                            )
                    ).replace(
                            "\n",
                            ""
                    ) // remove newline character
            );

        } else {
            output.setSubmissionOutput(null);
        }

        output.setResultStatus(
                request.getStatus()
                        .getDescription()
        );

        if (request.getStderr() != null) {
            output.setError(
                    new String(
                            decoder.decode(
                                    request.getStderr()
                                            .trim()
                                            .replaceAll(
                                                "\\s+",
                                                ""
                                            )
                            )
                    )
            );
        } else {
            output.setError(null);
        }

        if (request.getMessage()!=null) {
            output.setMessage(
                    new String(
                            decoder.decode(
                                    request.getMessage()
                                            .trim()
                                            .replaceAll(
                                                "\\s+",
                                                ""
                                            )
                            )
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
                    new String(
                            decoder.decode(
                                    request.getCompile_output()
                                            .trim()
                                            .replaceAll(
                                                "\\s+",
                                                ""
                                            )
                            )
                    )
            );
        } else {
            output.setCompileOutput(null);
        }

        output = testCaseRunCodeOutputRepository.save(output);

        return problemRunCodeMapper.toCreationProblemRunCodeResponse(output.getRunCode());
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
