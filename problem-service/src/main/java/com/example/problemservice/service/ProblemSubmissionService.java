package com.example.problemservice.service;

import com.example.problemservice.client.BoilerplateClient;
import com.example.problemservice.client.Judge0Client;
import com.example.problemservice.dto.request.ProblemSubmission.DetailsProblemSubmissionRequest;
import com.example.problemservice.dto.response.SubmissionCallbackResponse;
import com.example.problemservice.dto.response.problemSubmission.DetailsProblemSubmissionResponse;
import com.example.problemservice.exception.AppException;
import com.example.problemservice.exception.ErrorCode;
import com.example.problemservice.mapper.ProblemSubmissionMapper;
import com.example.problemservice.model.*;
import com.example.problemservice.model.composite.testCaseOutputId;
import com.example.problemservice.repository.*;
import com.example.problemservice.utils.ParseUUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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
    private final ProblemSubmissionMapper problemSubmissionMapper;
    private final BoilerplateClient boilerplateClient;
    private final ProgrammingLanguageRepository programmingLanguageRepository;
    private final TestCaseRepository testCaseRepository;

    public ProblemSubmission submitProblem(ProblemSubmission submission) {
        // Lấy Problem
        Problem problem = problemRepository.findById(submission.getProblem().getProblemId()).orElseThrow(
                () -> new AppException(ErrorCode.PROBLEM_NOT_EXIST)
        );
        submission.setProblem(problem);

        // Lưu ProblemSubmission trước để đảm bảo có ID
        submission = problemSubmissionRepository.save(submission);

        // Lấy danh sách TestCase từ Problem
        List<TestCase> testCases = problem.getTestCases();

        // Danh sách TestCase_Output để lưu kết quả
        List<TestCase_Output> outputs = new ArrayList<>();

        // Gửi từng test case đến Judge0 và xử lý kết quả
        for (TestCase testCase : testCases) {
            // Gửi mã nguồn và test case đến Judge0
            TestCase_Output output = judge0Client.submitCode(submission, testCase);


            // Khởi tạo composite ID
            testCaseOutputId outputId = new testCaseOutputId();
            outputId.setSubmission_id(submission.getSubmission_id());
            outputId.setTestcase_id(testCase.getTestcaseId());

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
        submission.setTestCases_output(outputs);

        // Lưu lại ProblemSubmission
        return problemSubmissionRepository.save(submission);
    }

    public ProblemSubmission callbackUpdate(SubmissionCallbackResponse request){
        TestCase_Output output = testCaseOutputRepository.findByToken(UUID.fromString(request.getToken())).orElseThrow(
                () -> new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION)
        );

        output.setRuntime(Float.valueOf(request.getTime()));
        output.setSubmission_output(request.getStdout());
        output.setResult_status(request.getStatus().getDescription());
        testCaseOutputRepository.save(output);

        return output.getSubmission();
    }

    public ProblemSubmission updateSubmissionResult(UUID submissionId) {
        // Lấy ProblemSubmission từ database
        ProblemSubmission submission = problemSubmissionRepository.findById(submissionId)
                .orElseThrow(() -> new AppException(ErrorCode.SUBMISSION_NOT_EXIST));

        // Lấy danh sách TestCase_Output liên quan đến submission
        List<TestCase_Output> outputs = submission.getTestCases_output();

        for (TestCase_Output output : outputs) {
            // Gửi yêu cầu lấy trạng thái từ Judge0
            TestCase_Output updatedOutput = judge0Client.getSubmissionResult(output);

            // Nếu kết quả vẫn còn "In Queue", bỏ qua việc cập nhật
            if ("In Queue".equals(updatedOutput.getResult_status())) {
                continue;
            }

            // Cập nhật thông tin mới từ Judge0 vào TestCase_Output
            output.setRuntime(updatedOutput.getRuntime());
            output.setSubmission_output(updatedOutput.getSubmission_output());
            output.setResult_status(updatedOutput.getResult_status());

            // Lưu kết quả cập nhật vào database
            testCaseOutputRepository.save(output);
        }

        // Trả về submission đã cập nhật kết quả
        return submission;
    }

    public ProblemSubmission getSubmission(UUID submissionId) {
        return problemSubmissionRepository.findById(submissionId)
                .orElseThrow(() -> new AppException(
                        ErrorCode.SUBMISSION_NOT_EXIST)
                );
    }

    public List<DetailsProblemSubmissionResponse> getSubmissionDetailsByProblemIdAndUserUid(UUID problemId, UUID userUid) {
        // Kiểm tra và lấy Problem từ repository
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new AppException(ErrorCode.PROBLEM_NOT_EXIST));

        log.info("User UID: {}", userUid);

        // Tìm kiếm submission
        List<ProblemSubmission> submissions = problemSubmissionRepository
                .findProblemSubmissionByProblemAndUserUid(problem, userUid)
                .orElse(Collections.emptyList()); // Trả về danh sách trống nếu không tìm thấy

        // Xử lý danh sách trống
        if (submissions.isEmpty()) {
            return Collections.emptyList(); // Trả về danh sách rỗng nếu không có submission nào
        }

        log.info("Submissions length: {}", submissions.size());

        // Chuyển đổi submissions thành response
        return submissions.stream()
                .map(problemSubmissionMapper::toDetailsProblemSubmissionResponse) // Gọi mapper từng đối tượng
                .collect(Collectors.toList());
    }

    public ProblemSubmission submitProblemWithPartialBoilerplate(DetailsProblemSubmissionRequest request) {
        Problem problem = problemRepository.findById(request.getProblemId())
                .orElseThrow(() -> new AppException(ErrorCode.PROBLEM_NOT_EXIST));

        ProgrammingLanguage language = programmingLanguageRepository.findById(request.getLanguageId())
                .orElseThrow(() -> new AppException(ErrorCode.PROGRAMMING_LANGUAGE_NOT_EXIST));


        UUID userUid = ParseUUID.normalizeUID(request.getUserUid());
            //UUID.fromString("4d0c8d27-4509-402b-cf6f-58686cd47319");

        List<DetailsProblemSubmissionResponse> submissions = getSubmissionDetailsByProblemIdAndUserUid(
                request.getProblemId(),
                userUid
        );

        ProblemSubmission submission = problemSubmissionMapper.toProblemSubmission(request);

        submission.setUserUid(userUid);

        submission.setProblem(problem);

        submission.setCode(
            boilerplateClient.enrich(
                    submission.getCode(),
                    request.getLanguageId(),
                    problem.getProblemStructure()
            )
        );

        submission.setProgramming_language(language.getLongName());

        submissions.sort(Comparator.comparingInt(DetailsProblemSubmissionResponse::getSubmissionOrder));

        submission.setScore_achieved(0);

        if (submissions.isEmpty()) {
            submission.setSubmit_order(0);
        } else {
            submission.setSubmit_order(
                submissions.get(submissions.size()-1) // Lấy submission cuối cùng
                        .getSubmissionOrder() + 1
            );
        }

        submission.setTestCases_output(new ArrayList<>());

        submission = problemSubmissionRepository.save(submission);


        List<TestCase> testCases = testCaseRepository.findAllByProblem_ProblemId(
                request.getProblemId()
        ); // problem.getTestCases();

        log.info("Test cases: {}", testCases.size());

        List<TestCase_Output> outputs = new ArrayList<>();

        for (TestCase testCase : testCases) {
            // Gửi mã nguồn và test case đến Judge0
            TestCase_Output output = judge0Client.submitCode(submission, testCase);

            // Khởi tạo composite ID
            testCaseOutputId outputId = new testCaseOutputId();
            outputId.setSubmission_id(submission.getSubmission_id());
            outputId.setTestcase_id(testCase.getTestcaseId());

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
        submission.setTestCases_output(outputs);

        // Lưu lại ProblemSubmission
        submission = problemSubmissionRepository.save(submission);

        return submission;
    }

}

