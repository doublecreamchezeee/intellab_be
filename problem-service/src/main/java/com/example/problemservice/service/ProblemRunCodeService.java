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

import java.util.*;

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

    public Hashtable<Integer, Boolean> getHashTableFromProblemCategories(List<ProblemCategory> problemCategories) {
        Hashtable<Integer, Boolean> categories = new Hashtable<>();
        if (problemCategories != null) {
            for (ProblemCategory problemCategory : problemCategories) {
                categories.put(problemCategory.getProblemCategoryID().getCategoryId(), true);
            }
        }
        return categories;
    }

    public String findCustomCheckerCodeByLanguageId(List<CustomCheckerCode> customCheckerCodes, int languageId) {
        for (CustomCheckerCode checkerCode : customCheckerCodes) {
            if (checkerCode.getCustomCheckerLanguageId() == languageId) {
                return checkerCode.getCustomCheckerCode();
            }
        }

        return "";
    }

    public CreationProblemRunCodeResponse runCode(UUID userId, DetailsProblemRunCodeRequest request, Boolean base64) {
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

        if (problem.getHasCustomChecker() != null && problem.getHasCustomChecker()) {
            log.info("Problem has custom checker, appending custom checker code.");
            String customCode = findCustomCheckerCodeByLanguageId(
                    problem.getCustomCheckerCodes(),
                    language.getId()
            );

            problemRunCode.setCode(
                    problemRunCode.getCode() + "\n" + customCode
            );
        } else {
            log.info("Problem does not have custom checker, skipping custom checker code.");
        }

        log.info("Enriching code with boilerplate for problem ID: {}, {}", request.getProblemId(), problemRunCode.getCode());

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

        problemRunCode.setProgrammingLanguage(language.getLongName());

        problemRunCode = problemRunCodeRepository.save(problemRunCode);

        List<TestCase> testCases = testCaseRepository.findAllByProblem_ProblemId(
                request.getProblemId()
        );

        List<TestCaseRunCodeOutput> outputs = new ArrayList<>();

        for (int i = 0; i < 3 && i < NUMBER_OF_TEST_CASE; i++) {
            TestCase testCase = testCases.get(i);

            TestCaseRunCodeOutput output = judge0Client.runCode(
                    problemRunCode,
                    testCase,
                    problem.getHasCustomChecker()
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

    public CreationProblemRunCodeResponse runCodeBatch(UUID userId, DetailsProblemRunCodeRequest request, Boolean base64) {
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

        if (problem.getHasCustomChecker() != null && problem.getHasCustomChecker()) {
            log.info("Problem has custom checker, appending custom checker code.");
            String customCode = findCustomCheckerCodeByLanguageId(
                    problem.getCustomCheckerCodes(),
                    language.getId()
            );

            problemRunCode.setCode(
                    problemRunCode.getCode() + "\n" + customCode
            );
        } else {
            log.info("Problem does not have custom checker, skipping custom checker code.");
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


        log.info("Enriching code with boilerplate for problem ID: {}, {}", request.getProblemId(), problemRunCode.getCode());

        problemRunCode.setProgrammingLanguage(language.getLongName());

        problemRunCode = problemRunCodeRepository.save(problemRunCode);

        List<TestCase> testCases = testCaseRepository.findAllByProblem_ProblemId(
                request.getProblemId()
        );

        int maxSize = testCases.size();
        // Limit the number of test cases to NUMBER_OF_TEST_CASE
        if (testCases.size() > NUMBER_OF_TEST_CASE) {
            maxSize = NUMBER_OF_TEST_CASE;
        }
        testCases = testCases.subList(0, Math.min(3, maxSize));

        List<TestCaseRunCodeOutput> outputs = judge0Client.runCodeBatch(
                problemRunCode,
                testCases,
                problem.getHasCustomChecker()
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
            output.setHasCustomChecker(problem.getHasCustomChecker());
            output.setIsPassedByCheckingCustomChecker(null);

            output = testCaseRunCodeOutputRepository.save(output);

            outputs.set(i, output);
        }

        problemRunCode.setTestCasesRunCodeOutput(outputs);

        problemRunCode = problemRunCodeRepository.save(problemRunCode);

        return problemRunCodeMapper.toCreationProblemRunCodeResponse(problemRunCode);
    }

    private boolean parseLastLineAsBoolean(String input) {
        String[] lines = input.split("\\R"); // \R matches any line break
        if (lines.length == 0) return false;

        String lastLine = lines[lines.length - 1].trim();
        return Boolean.parseBoolean(lastLine);
    }

    private String removeLastLine(String input) {
        String[] lines = input.split("\\R"); // \R matches any line break
        if (lines.length <= 1) return ""; // No line or only one line

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.length - 1; i++) {
            sb.append(lines[i]);
            if (i != lines.length - 2) {
                sb.append(System.lineSeparator());
            }
        }
        return sb.toString();
    }

    public CreationProblemRunCodeResponse callbackUpdate(SubmissionCallbackResponse request, Boolean hasCustomChecker) {
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

        output.setStatusId(
                request.getStatus()
                        .getId()
        );

        if (hasCustomChecker != null && hasCustomChecker) {
            if (request.getStdout() != null) {
                String outputWithCustomCheckerResult =
                        new String(
                                decoder.decode(
                                        request.getStdout()
                                                .trim()
                                                .replaceAll(
                                                    "\\s+",
                                                    ""
                                                )
                                )
                        );/*.replace(
                                "\n",
                                ""
                        ); */// remove newline character

                boolean isPassed = parseLastLineAsBoolean(outputWithCustomCheckerResult);
                output.setIsPassedByCheckingCustomChecker(
                        isPassed
                );

                output.setSubmissionOutput(
                        removeLastLine(outputWithCustomCheckerResult).replace(
                                "\n",
                                ""
                        )
                );

                output.setStatusId(
                        isPassed ? 3 : 4 // Assuming 3 is "Accepted" and 4 is "Wrong Answer"
                );

                output.setResultStatus(
                        isPassed ? "Accepted" : "Wrong Answer"
                );


            } else {
                output.setSubmissionOutput(null);
            }

        } else {
            output.setSubmissionOutput(null);
            /*output.setActualOutput(null);
            output.setExpectedOutput(null);*/
        }

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
