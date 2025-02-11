package com.example.problemservice.service;

import com.example.problemservice.client.BoilerplateClient;
import com.example.problemservice.client.CourseClient;
import com.example.problemservice.client.Judge0Client;
import com.example.problemservice.dto.request.ProblemSubmission.DetailsProblemSubmissionRequest;
import com.example.problemservice.dto.request.lesson.DonePracticeRequest;
import com.example.problemservice.dto.response.SubmissionCallbackResponse;
import com.example.problemservice.dto.response.problemSubmission.DetailsProblemSubmissionResponse;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final CourseClient courseClient;

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
        return problemSubmissionRepository.save(submission);
    }

    public ProblemSubmission callbackUpdate(SubmissionCallbackResponse request){
        TestCaseOutput output = testCaseOutputRepository.findByToken(UUID.fromString(request.getToken())).orElseThrow(
                () -> new AppException(ErrorCode.TEST_CASE_OUTPUT_NOT_EXIST)
        );

        output.setRuntime(Float.valueOf(request.getTime()));
//        output.setSubmission_output(request.getStdout());
        output.setSubmission_output(new String(Base64.getDecoder().decode(request.getStdout().trim().replaceAll("\\s+", ""))));
        output.setResult_status(request.getStatus().getDescription());
        testCaseOutputRepository.save(output);

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

    public ProblemSubmission getSubmission(UUID submissionId) {
        // Fetch the submission
        ProblemSubmission submission = problemSubmissionRepository.findById(submissionId)
                .orElseThrow(() -> new AppException(ErrorCode.SUBMISSION_NOT_EXIST));

        // Check if all associated test cases have result_status as "Accepted"
        boolean allAccepted = submission.getTestCasesOutput().stream()
                .allMatch(testCaseOutput -> "Accepted".equals(testCaseOutput.getResult_status()));

        if (allAccepted) {
            submission.setIsSolved(true);
            problemSubmissionRepository.save(submission);

            courseClient.donePracticeByProblemId(
                    submission.getProblem().getProblemId(),
                    submission.getUserId()
            );

        }

        return submission;
    }

    public List<DetailsProblemSubmissionResponse> getSubmissionDetailsByProblemIdAndUserUid(UUID problemId, UUID userUid) {
        // Kiểm tra và lấy Problem từ repository
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new AppException(ErrorCode.PROBLEM_NOT_EXIST));

        // Tìm kiếm submission
        List<ProblemSubmission> submissions = problemSubmissionRepository
                .findProblemSubmissionByProblemAndUserId(problem, userUid)
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

    public ProblemSubmission submitProblemWithPartialBoilerplate(UUID userUid, DetailsProblemSubmissionRequest request) {
        Problem problem = problemRepository.findById(request.getProblemId())
                .orElseThrow(() -> new AppException(ErrorCode.PROBLEM_NOT_EXIST));

        ProgrammingLanguage language = programmingLanguageRepository.findById(request.getLanguageId())
                .orElseThrow(() -> new AppException(ErrorCode.PROGRAMMING_LANGUAGE_NOT_EXIST));


        //UUID userUid = ParseUUID.normalizeUID(userUid);
            //UUID.fromString("4d0c8d27-4509-402b-cf6f-58686cd47319");

        List<DetailsProblemSubmissionResponse> submissions = getSubmissionDetailsByProblemIdAndUserUid(
                request.getProblemId(),
                userUid
        );

        ProblemSubmission submission = problemSubmissionMapper.toProblemSubmission(request);

        submission.setUserId(userUid);

        submission.setProblem(problem);

        submission.setCode(
            boilerplateClient.enrich(
                    submission.getCode(),
                    request.getLanguageId(),
                    problem.getProblemStructure()
            )
        );

        submission.setProgrammingLanguage(language.getLongName());

        submissions.sort(Comparator.comparingInt(DetailsProblemSubmissionResponse::getSubmissionOrder));

        submission.setScoreAchieved(0);

        if (submissions.isEmpty()) {
            submission.setSubmitOrder(0);
        } else {
            submission.setSubmitOrder(
                submissions.get(submissions.size()-1) // Lấy submission cuối cùng
                        .getSubmissionOrder() + 1
            );
        }

        submission.setTestCasesOutput(new ArrayList<>());

        submission = problemSubmissionRepository.save(submission);


        List<TestCase> testCases = testCaseRepository.findAllByProblem_ProblemId(
                request.getProblemId()
        ); // problem.getTestCases();

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

