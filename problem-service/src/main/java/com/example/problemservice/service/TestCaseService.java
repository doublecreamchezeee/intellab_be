package com.example.problemservice.service;

import com.example.problemservice.constants.SupportedDataType;
import com.example.problemservice.dto.request.TestCaseCreationRequest;
import com.example.problemservice.dto.request.testcase.TestCaseMultipleCreationRequest;
import com.example.problemservice.exception.AppException;
import com.example.problemservice.exception.ErrorCode;
import com.example.problemservice.model.Problem;
import com.example.problemservice.model.TestCase;
import com.example.problemservice.repository.ProblemRepository;
import com.example.problemservice.repository.TestCaseRepository;
import com.example.problemservice.utils.TestCaseFileReader;

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
    public TestCase createTestCase(UUID userUid, TestCaseCreationRequest request) {
        Problem problem = problemRepository.findById(request.getProblemId()).orElseThrow(
                () -> new AppException(ErrorCode.PROBLEM_NOT_EXIST));

        TestCase testCase = TestCase.builder()
                .problem(problem)
                .input(request.getInput())
                .output(request.getOutput())
                .userId(userUid)
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

        testCase = testCaseRepository.save(testCase);

        TestCaseFileReader.saveOneTestCase(
                problem.getProblemName(),
                testCase.getInput(),
                testCase.getOutput()
        );

        return testCase;
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

    public List<TestCase> createMultipleTestCases(UUID userUid, TestCaseMultipleCreationRequest request) {
        Problem problem = problemRepository.findById(request.getProblemId()).orElseThrow(
                () -> new AppException(ErrorCode.PROBLEM_NOT_EXIST));

        if (request.getInputs().size() != request.getOutputs().size()) {
            throw new IllegalArgumentException("Inputs and outputs lists must have the same size");
        }

        List<TestCase> testCases = new ArrayList<>();
        for (int i = 0; i < request.getInputs().size(); i++) {
            TestCase testCase = TestCase.builder()
                    .problem(problem)
                    .input(request.getInputs().get(i))
                    .output(request.getOutputs().get(i))
                    .userId(userUid)
                    .build();
            testCases.add(testCase);
        }

        if (problem.getTestCases() != null){
            List<TestCase> newListTestCase = problem.getTestCases();
            newListTestCase.addAll(testCases);
            problem.setTestCases(newListTestCase);
        }
        else {
            problem.setTestCases(testCases);
        }

        testCaseRepository.saveAll(testCases);

        // Save test cases as .txt files
        TestCaseFileReader.saveTestCases(
                problem.getProblemName(),
                request.getInputs(),
                request.getOutputs());

        return testCases;
    }

    public List<String> getSupportedDataTypes() {
        return SupportedDataType.getSupportedDataTypes();
    }
}