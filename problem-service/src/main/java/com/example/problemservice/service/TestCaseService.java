package com.example.problemservice.service;

import com.example.problemservice.dto.request.TestCaseCreationRequest;
import com.example.problemservice.exception.AppException;
import com.example.problemservice.exception.ErrorCode;
import com.example.problemservice.model.Problem;
import com.example.problemservice.model.TestCase;
import com.example.problemservice.repository.ProblemRepository;
import com.example.problemservice.repository.TestCaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TestCaseService {
    private final TestCaseRepository testCaseRepository;
    private final ProblemRepository problemRepository;
    public TestCase createTestCase(TestCaseCreationRequest request) {
        Problem problem = problemRepository.findById(request.getProblemId()).orElseThrow(
                () -> new AppException(ErrorCode.PROBLEM_NOT_EXIST));

        TestCase testCase = TestCase.builder()
                .problem(problem)
                .input(request.getInput())
                .output(request.getOutput())
                .userId(request.getUserId())
                .build();

        if (problem.getTestCases() != null){
            List<TestCase> newListTestCase = problem.getTestCases();
            newListTestCase.add(testCase);
            problem.setTestCases(newListTestCase);
        }
        else {
            List<TestCase> newListTestCase = new ArrayList<>();
            newListTestCase.add(testCase);
            problem.setTestCases(newListTestCase);
        }

        return testCaseRepository.save(testCase);
    }

    public TestCase getTestCase(UUID testCaseId) {
        return testCaseRepository.findById(testCaseId)
                .orElseThrow(() -> new AppException(
                        ErrorCode.TESTCASE_NOT_EXIST)
                );
    }

    public List<TestCase> getAllTestCases() {
        return testCaseRepository.findAll();
    }

    public List<TestCase> getTestCasesByProblemId(UUID problemId) {
        return testCaseRepository.findAllByProblem_ProblemId(problemId);
    }
}