package com.example.problemservice.service;

import com.example.problemservice.dto.response.ApiResponse;
import com.example.problemservice.dto.response.profile.SingleProfileInformationResponse;
import com.example.problemservice.client.*;
import com.example.problemservice.dto.request.LeaderboardUpdateRequest;
import com.example.problemservice.dto.request.ProblemSubmission.DetailsProblemSubmissionRequest;
import com.example.problemservice.dto.request.ProblemSubmission.MossRequest;
import com.example.problemservice.dto.request.ProblemSubmission.SubmitCodeRequest;
import com.example.problemservice.dto.request.lesson.DonePracticeRequest;
import com.example.problemservice.dto.request.profile.SingleProfileInformationRequest;
import com.example.problemservice.dto.response.Problem.CategoryResponse;
import com.example.problemservice.dto.response.SubmissionCallbackResponse;
import com.example.problemservice.dto.response.problemSubmission.DetailsProblemSubmissionResponse;
import com.example.problemservice.dto.response.problemSubmission.MossMatchResponse;
import com.example.problemservice.dto.response.problemSubmission.ProblemSubmissionResponse;
import com.example.problemservice.exception.AppException;
import com.example.problemservice.exception.ErrorCode;
import com.example.problemservice.mapper.ProblemSubmissionMapper;
import com.example.problemservice.model.*;
import com.example.problemservice.model.composite.TestCaseOutputID;
import com.example.problemservice.repository.*;
import com.example.problemservice.utils.ParseUUID;
import com.example.problemservice.model.Problem;
import com.example.problemservice.model.ProblemSubmission;
import com.example.problemservice.model.TestCase;
import com.example.problemservice.model.TestCaseOutput;
import com.example.problemservice.model.composite.TestCaseOutputID;
import com.example.problemservice.repository.ProblemRepository;
import com.example.problemservice.repository.ProblemSubmissionRepository;
import com.example.problemservice.repository.TestCaseOutputRepository;
//import com.example.problemservice.utils.Base64Util;
import lombok.RequiredArgsConstructor;
import org.hibernate.type.ListType;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProblemSubmissionService {
    private static final Logger log = LoggerFactory.getLogger(ProblemSubmissionService.class);
    private final ProblemSubmissionRepository problemSubmissionRepository;
    private final ProblemRepository problemRepository;
    private final TestCaseOutputRepository testCaseOutputRepository;
    private final Judge0Client judge0Client;
    private final MossClient mossClient;
    private final ProblemSubmissionMapper problemSubmissionMapper;
    private final BoilerplateClient boilerplateClient;
    private final ProgrammingLanguageRepository programmingLanguageRepository;
    private final TestCaseRepository testCaseRepository;
    private final CourseClient courseClient;
    private final IdentityClient identityClient;
    private final NotificationService notificationService;
    private final ViewSolutionBehaviorRepository viewSolutionBehaviorRepository;

    public DetailsProblemSubmissionResponse submitProblem(SubmitCodeRequest request, Boolean base64) {

        // Lấy Problem
        Problem problem = problemRepository.findById(UUID.fromString(request.getProblemId())).orElseThrow(
                () -> new AppException(ErrorCode.PROBLEM_NOT_EXIST));

        ProgrammingLanguage language = programmingLanguageRepository.findByLongName(request.getProgrammingLanguage())
                .orElseThrow(() -> new AppException(ErrorCode.PROGRAMMING_LANGUAGE_NOT_EXIST));

        if (base64 != null && base64) {
            Base64.Decoder decoder = Base64.getDecoder();
            request.setCode(
                    new String(
                            decoder.decode(
                                    request.getCode())));
        }

        ProblemSubmission submission = ProblemSubmission.builder()
                .code(boilerplateClient.enrich(
                        request.getCode(),
                        language.getId(),
                        problem.getProblemStructure()))
                .createdAt(new Date())
                .isSolved(false)
                .programmingLanguage(request.getProgrammingLanguage())
                .submitOrder(request.getSubmitOrder())
                .userId(ParseUUID.normalizeUID(request.getUserId()))
                .build();

        submission.setProblem(problem);

        // Lưu ProblemSubmission trước để đảm bảo có ID
        submission = problemSubmissionRepository.save(submission);

        // Lấy danh sách TestCase từ Problem
        List<TestCase> testCases = problem.getTestCases();

        // Danh sách TestCaseOutput để lưu kết quả
        List<TestCaseOutput> outputs = new ArrayList<>();

        // Gửi từng test case đến Judge0 và xử lý kết quả
        for (TestCase testCase : testCases) {
            // Gửi mã nguồn và test case đến Judge0
            TestCaseOutput output = judge0Client.submitCode(submission, testCase);

            // Khởi tạo composite ID
            TestCaseOutputID outputId = new TestCaseOutputID();
            outputId.setSubmissionId(submission.getSubmissionId());
            outputId.setTestcaseId(testCase.getTestcaseId());

            // Gán composite ID và liên kết với ProblemSubmission
            output.setTestCaseOutputID(outputId);
            output.setTestcase(testCase);
            output.setSubmission(submission);

            // Lưu TestCaseOutput
            output = testCaseOutputRepository.save(output);

            // Thêm vào danh sách kết quả
            outputs.add(output);
        }

        // Gắn danh sách kết quả vào submission
        submission.setTestCasesOutput(outputs);

        // Lưu lại ProblemSubmission
        ProblemSubmission result = problemSubmissionRepository.save(submission);

        return getDetailsProblemSubmissionResponse(result);
    }

    public List<MossMatchResponse> mossService(UUID submissionId) throws IOException, InterruptedException {
        ProblemSubmission submission = problemSubmissionRepository.findById(submissionId)
                .orElseThrow(() -> new AppException(ErrorCode.SUBMISSION_NOT_EXIST));

        if (submission.getIsCheckedMoss() == null || !submission.getIsCheckedMoss()) {
            submission.setMossReportUrl(checkMoss(submission.getProgrammingLanguage(),
                    submission.getProblem().getProblemId(), submission.getUserId()));
            submission.setIsCheckedMoss(true);
            problemSubmissionRepository.save(submission);
        }
        try {
            String resultHtml = mossClient.fetchMossHtml(submission.getMossReportUrl());
            List<MossMatchResponse> responses = mossClient.parseMossHtml(resultHtml,
                    String.valueOf(submission.getSubmissionId()));

            return responses.stream().map(moss -> {
                ApiResponse<SingleProfileInformationResponse> response = identityClient
                        .getSingleProfileInformationById(new SingleProfileInformationRequest(moss.getUserId2()))
                        .block();
                if (response != null && response.getResult() == null) {
                    return moss;
                }
                SingleProfileInformationResponse profile = response.getResult();
                System.out.print("Display name: " +  profile.getDisplayName());
                moss.setUsername2(profile.getDisplayName());
                return moss;
            }).toList();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String checkMoss(String language, UUID problemId, UUID userId) throws IOException, InterruptedException {
        List<ProblemSubmission> acceptedSubmissions = problemSubmissionRepository
                .findTop5ByIsSolvedAndProgrammingLanguageAndProblem_ProblemIdOrderByCreatedAtDesc(true, language, problemId);

        List<MossRequest> requests = acceptedSubmissions.stream().map(submission -> {
            String acceptedLanguage = submission.getProgrammingLanguage().toLowerCase();
            String funcCode = boilerplateClient.extractFunctionCode(
                    submission.getCode(),
                    acceptedLanguage,
                    submission.getProblem().getProblemStructure());
            return MossRequest.builder()
                    .functionCode(funcCode)
                    .submissionId(submission.getSubmissionId())
                    .userId(submission.getUserId())
                    .build();
        }).toList();

        return mossClient.moss(requests, boilerplateClient.normalizeLanguage(language));
    }

    @NotNull
    private DetailsProblemSubmissionResponse getDetailsProblemSubmissionResponse(ProblemSubmission result) {
        DetailsProblemSubmissionResponse response = problemSubmissionMapper.toDetailsProblemSubmissionResponse(result);
        List<TestCaseOutput> testCaseOutputs = result.getTestCasesOutput();
        response.setSubmitDate(result.getCreatedAt());

        if (testCaseOutputs != null && !testCaseOutputs.isEmpty()) {
            Float totalMemories = (float) result.getTestCasesOutput().stream()
                    .mapToDouble(testCaseOutput -> testCaseOutput.getMemory() != null ? testCaseOutput.getMemory() : 0)
                    .sum();
            response.setUsedMemory(totalMemories);
            Float totalRuntime = (float) result.getTestCasesOutput().stream()
                    .mapToDouble(
                            testCaseOutput -> testCaseOutput.getRuntime() != null ? testCaseOutput.getRuntime() : 0)
                    .sum();
            response.setRuntime(totalRuntime);
        }

        List<ProblemCategory> problemCategories = result.getProblem().getCategories();

        List<CategoryResponse> categories = problemCategories.stream()
                .map(p -> courseClient.categories(p.getProblemCategoryID().getCategoryId()).getResult())
                .toList();
        response.getProblem().setCategories(categories);
        return response;
    }

    public ProblemSubmission callbackUpdate(SubmissionCallbackResponse request) {
        TestCaseOutput output = testCaseOutputRepository.findByToken(UUID.fromString(request.getToken())).orElseThrow(
                () -> new AppException(ErrorCode.TEST_CASE_OUTPUT_NOT_EXIST));

        if (request.getTime() != null) {
            output.setRuntime(Float.valueOf(request.getTime()));
        } else {
            output.setRuntime(null);
        }

        output.setMemory(request.getMemory());
        Base64.Decoder decoder = Base64.getDecoder();

        if (request.getStdout() != null) {

            output.setSubmission_output(
                    new String(
                            decoder.decode(
                                    request.getStdout()
                                            .trim()
                                            .replaceAll(
                                                    "\\s+",
                                                    "")))
                            .replace(
                                    "\n",
                                    "") // remove newline character
            );

        } else {
            output.setSubmission_output(null);
        }
        // output.setSubmission_output(new
        // String(Base64.getDecoder().decode(request.getStdout().trim().replaceAll("\\s+",
        // ""))));
        output.setResult_status(request.getStatus().getDescription());
        testCaseOutputRepository.save(output);

        // if (result.getTestCasesOutput().size() ==
        // result.getProblem().getTestCases().size()) {
        // boolean allAccepted = result.getTestCasesOutput().stream()
        // .allMatch(testCaseOutput ->
        // "Accepted".equals(testCaseOutput.getResult_status()));
        //
        // if (allAccepted) {
        // if (!result.getIsSolved())
        // {
        //
        // }
        //
        // try {
        // courseClient.donePracticeByProblemId(
        // result.getProblem().getProblemId(),
        // result.getUserId()
        // );
        // } catch (Exception e) {
        // log.error("Error while calling course service: {}", e.getMessage());
        // }
        //
        // }
        // }

        return output.getSubmission();
    }

    public ProblemSubmission updateSubmissionResult(UUID submissionId) {
        // Lấy ProblemSubmission từ database
        ProblemSubmission submission = problemSubmissionRepository.findById(submissionId)
                .orElseThrow(() -> new AppException(ErrorCode.SUBMISSION_NOT_EXIST));

        // Lấy danh sách TestCaseOutput liên quan đến submission
        List<TestCaseOutput> outputs = submission.getTestCasesOutput();

        for (TestCaseOutput output : outputs) {
            // Gửi yêu cầu lấy trạng thái từ Judge0
            TestCaseOutput updatedOutput = judge0Client.getSubmissionResult(output);

            // Nếu kết quả vẫn còn "In Queue", bỏ qua việc cập nhật
            if ("In Queue".equals(updatedOutput.getResult_status())) {
                continue;
            }

            // Cập nhật thông tin mới từ Judge0 vào TestCaseOutput
            output.setRuntime(updatedOutput.getRuntime());
            output.setSubmission_output(updatedOutput.getSubmission_output());
            output.setResult_status(updatedOutput.getResult_status());

            // Lưu kết quả cập nhật vào database
            testCaseOutputRepository.save(output);
        }

        // Trả về submission đã cập nhật kết quả
        return submission;
    }

    public Page<ProblemSubmissionResponse> getSubmissionsByUserId(UUID problemId, UUID userId, Pageable pageable) {
        Page<ProblemSubmission> submissions = problemSubmissionRepository.findAllByUserIdAndProblem_ProblemId(userId,
                problemId, pageable);

        return submissions.map(submission -> {
            double totalRuntime = submission.getTestCasesOutput().stream()
                    .mapToDouble(TestCaseOutput::getRuntime)
                    .sum();

            double totalMemory = submission.getTestCasesOutput().stream()
                    .mapToDouble(TestCaseOutput::getMemory)
                    .sum();

            boolean allAccepted = submission.getTestCasesOutput().stream()
                    .allMatch(output -> "Accepted".equals(output.getResult_status()));

            String status = "Failed";

            if (allAccepted) {
                status = "Accepted";
                if (submission.getIsSolved() == null || !submission.getIsSolved()) {
                    submission.setIsSolved(true);
                    notificationService.solveProblemNotification(submission.getProblem(), submission.getUserId());
                    problemSubmissionRepository.save(submission);
                }
            }

            return ProblemSubmissionResponse.builder()
                    .submissionId(submission.getSubmissionId().toString())
                    .programmingLanguage(submission.getProgrammingLanguage())
                    .runtime(totalRuntime)
                    .memory(totalMemory)
                    .submitDate(submission.getCreatedAt())
                    .status(status)
                    .build();
        });
    }

    public DetailsProblemSubmissionResponse getSubmission(UUID submissionId) {
        // Fetch the submission
        ProblemSubmission submission = problemSubmissionRepository.findById(submissionId)
                .orElseThrow(() -> new AppException(ErrorCode.SUBMISSION_NOT_EXIST));

        // Check if all associated test cases have result_status as "Accepted"
        boolean allAccepted = submission.getTestCasesOutput().stream()
                .allMatch(testCaseOutput -> "Accepted".equals(testCaseOutput.getResult_status()));

        if (allAccepted) {
            if (!submission.getIsSolved()) {
                submission.setIsSolved(true);
                problemSubmissionRepository.save(submission);
                notificationService.solveProblemNotification(submission.getProblem(), submission.getUserId());
            }

            try {
                courseClient.donePracticeByProblemId(
                        submission.getProblem().getProblemId(),
                        submission.getUserId());
            } catch (Exception e) {
                log.error("Error while calling course service: {}", e.getMessage());
            }

        }

        return getDetailsProblemSubmissionResponse(submission);
    }

    public List<DetailsProblemSubmissionResponse> getSubmissionDetailsByProblemIdAndUserUidInList(UUID problemId,
            UUID userUid) {
        // Kiểm tra và lấy Problem từ repository
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new AppException(ErrorCode.PROBLEM_NOT_EXIST));

        // Tìm kiếm submission
        List<ProblemSubmission> submissions = problemSubmissionRepository
                .findProblemSubmissionByProblemAndUserId(problem, userUid);

        // Chuyển đổi submissions thành response
        return submissions.stream()
                .map(this::getDetailsProblemSubmissionResponse)
                .collect(Collectors.toList());
    }

    public Page<DetailsProblemSubmissionResponse> getSubmissionDetailsByProblemIdAndUserUid(UUID problemId,
            UUID userUid, Pageable pageable) {
        // Kiểm tra và lấy Problem từ repository
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new AppException(ErrorCode.PROBLEM_NOT_EXIST));

        // Tìm kiếm submission
        Page<ProblemSubmission> submissions = problemSubmissionRepository
                .findProblemSubmissionByProblemAndUserId(problem, userUid, pageable);

        // Chuyển đổi submissions thành response
        return submissions
                .map(this::getDetailsProblemSubmissionResponse);
    }

    public Page<DetailsProblemSubmissionResponse> getSubmissionDetailsByUserUid(UUID userUid, Pageable pageable) {
        Page<ProblemSubmission> submissions = problemSubmissionRepository
                .findProblemSubmissionByUserId(userUid, pageable);

        return submissions
                .map(this::getDetailsProblemSubmissionResponse);
    }

    public ProblemSubmission submitProblemWithPartialBoilerplate(UUID userUid,
            DetailsProblemSubmissionRequest request) {
        Problem problem = problemRepository.findById(request.getProblemId())
                .orElseThrow(() -> new AppException(ErrorCode.PROBLEM_NOT_EXIST));

        ProgrammingLanguage language = programmingLanguageRepository.findById(request.getLanguageId())
                .orElseThrow(() -> new AppException(ErrorCode.PROGRAMMING_LANGUAGE_NOT_EXIST));

        // UUID userUid = ParseUUID.normalizeUID(userUid);
        // UUID.fromString("4d0c8d27-4509-402b-cf6f-58686cd47319");

        List<DetailsProblemSubmissionResponse> submissions = getSubmissionDetailsByProblemIdAndUserUidInList(
                request.getProblemId(),
                userUid);

        ProblemSubmission submission = problemSubmissionMapper.toProblemSubmission(request);

        submission.setUserId(userUid);

        submission.setProblem(problem);

        submission.setCode(
                boilerplateClient.enrich(
                        submission.getCode(),
                        request.getLanguageId(),
                        problem.getProblemStructure()));

        submission.setProgrammingLanguage(language.getLongName());

        submissions.sort(Comparator.comparingInt(DetailsProblemSubmissionResponse::getSubmissionOrder));

        submission.setScoreAchieved(0);

        if (submissions.isEmpty()) {
            submission.setSubmitOrder(0);
        } else {
            submission.setSubmitOrder(
                    submissions.get(submissions.size() - 1) // Lấy submission cuối cùng
                            .getSubmissionOrder() + 1);
        }

        submission.setTestCasesOutput(new ArrayList<>());

        submission = problemSubmissionRepository.save(submission);

        List<TestCase> testCases = testCaseRepository.findAllByProblem_ProblemId(
                request.getProblemId()); // problem.getTestCases();

        log.info("Test cases: {}", testCases.size());

        List<TestCaseOutput> outputs = new ArrayList<>();

        for (TestCase testCase : testCases) {
            // Gửi mã nguồn và test case đến Judge0
            TestCaseOutput output = judge0Client.submitCode(submission, testCase);

            // Khởi tạo composite ID
            TestCaseOutputID outputId = new TestCaseOutputID();
            outputId.setSubmissionId(submission.getSubmissionId());
            outputId.setTestcaseId(testCase.getTestcaseId());

            // Gán composite ID và liên kết với ProblemSubmission
            output.setTestCaseOutputID(outputId);
            output.setTestcase(testCase);
            output.setSubmission(submission);

            // Lưu TestCase_Output
            output = testCaseOutputRepository.save(output);

            // Thêm vào danh sách kết quả
            outputs.add(output);
        }

        // Gắn danh sách kết quả vào submission
        submission.setTestCasesOutput(outputs);

        // Lưu lại ProblemSubmission
        submission = problemSubmissionRepository.save(submission);

        return submission;
    }

}
