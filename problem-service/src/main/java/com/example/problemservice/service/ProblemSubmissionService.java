package com.example.problemservice.service;

import com.example.problemservice.client.Judge0Client;
import com.example.problemservice.dto.response.problemSubmission.DetailsProblemSubmissionResponse;
import com.example.problemservice.exception.AppException;
import com.example.problemservice.exception.ErrorCode;
import com.example.problemservice.mapper.ProblemSubmissionMapper;
import com.example.problemservice.model.Problem;
import com.example.problemservice.model.ProblemSubmission;
import com.example.problemservice.model.TestCase;
import com.example.problemservice.model.TestCase_Output;
import com.example.problemservice.model.composite.testCaseOutputId;
import com.example.problemservice.repository.ProblemRepository;
import com.example.problemservice.repository.ProblemSubmissionRepository;
import com.example.problemservice.repository.TestCaseOutputRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProblemSubmissionService {
    private final ProblemSubmissionRepository problemSubmissionRepository;
    private final ProblemRepository problemRepository;
    private final TestCaseOutputRepository testCaseOutputRepository;
    private final Judge0Client judge0Client;
    private final ProblemSubmissionMapper problemSubmissionMapper;

    public ProblemSubmission submitProblem(ProblemSubmission submission) {
        // Lấy Problem
        Problem problem = problemRepository.findById(submission.getProblem().getProblem_id()).orElseThrow(
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
            outputId.setTestcase_id(testCase.getTestcase_id());

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

    public DetailsProblemSubmissionResponse getSubmissionDetailsByProblemIdAndUserUid(UUID problemId, UUID userUid) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new AppException(ErrorCode.PROBLEM_NOT_EXIST));

        ProblemSubmission submission = problemSubmissionRepository.findProblemSubmissionByProblemAndUserUid(
                problem, userUid
            ).orElseThrow(() -> new AppException(ErrorCode.SUBMISSION_NOT_EXIST));

        return problemSubmissionMapper.toDetailsProblemSubmissionResponse(submission);
    }
}

