package com.example.problemservice.service;

import com.example.problemservice.constants.SupportedDataType;
import com.example.problemservice.core.NumberTestCaseGenerator;
import com.example.problemservice.dto.request.TestCaseCreationRequest;
import com.example.problemservice.dto.request.testcase.TestCaseMultipleCreationRequest;
import com.example.problemservice.dto.request.testcase.TestCasesGenerationRequest;
import com.example.problemservice.dto.response.testcase.TestCaseCreationResponse;
import com.example.problemservice.exception.AppException;
import com.example.problemservice.exception.ErrorCode;
import com.example.problemservice.mapper.TestCaseMapper;
import com.example.problemservice.model.Problem;
import com.example.problemservice.model.TestCase;
import com.example.problemservice.repository.ProblemRepository;
import com.example.problemservice.repository.TestCaseRepository;
import com.example.problemservice.utils.TestCaseFileReader;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TestCaseService {
    private final TestCaseRepository testCaseRepository;
    private final ProblemRepository problemRepository;
    private final TestCaseMapper testCaseMapper;
    
    public TestCaseCreationResponse createTestCase(Boolean isCreate, TestCaseCreationRequest request) {
        Problem problem = problemRepository.findById(request.getProblemId()).orElseThrow(
                () -> new AppException(ErrorCode.PROBLEM_NOT_EXIST));

        TestCase testCase = TestCase.builder()
                .problem(problem)
                .input(request.getInput())
                .output(request.getOutput())
                .order(request.getOrder())
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

        if (isCreate || problem.getCurrentCreationStep() <= 4){
            problem.setCurrentCreationStep(4);
            problem.setCurrentCreationStepDescription("Testcase Step");
        }
        problemRepository.save(problem);

        testCase = testCaseRepository.save(testCase);

        TestCaseFileReader.saveOneTestCase(
                problem.getProblemName(),
                testCase.getInput(),
                testCase.getOutput()
        );
        return testCaseMapper.toTestCaseResponse(testCase);
    }

    public TestCaseCreationResponse updateTestCase(UUID testCaseId, TestCaseCreationRequest request) {
        TestCase testCase = testCaseRepository.findById(testCaseId).orElseThrow(
                () -> new AppException(ErrorCode.TESTCASE_NOT_EXIST));

        Problem problem = testCase.getProblem();

        if (problem == null) {
            throw new AppException(ErrorCode.PROBLEM_NOT_EXIST);
        }

        if (!problem.getIsCompletedCreation() && problem.getCurrentCreationStep() <= 4){
            problem.setCurrentCreationStep(4);
            problem.setCurrentCreationStepDescription("Testcase Step");
        }
        problemRepository.save(problem);

        // Update fields
        testCase.setInput(request.getInput());
        testCase.setOutput(request.getOutput());
        testCase.setOrder(request.getOrder());

        // Save updated test case
        testCase = testCaseRepository.save(testCase);

        // Re-write updated testcase to file (overwrite)
        TestCaseFileReader.saveOneTestCase(
                problem.getProblemName(),
                testCase.getInput(),
                testCase.getOutput()
        );

        return testCaseMapper.toTestCaseResponse(testCase);
    }

    @Transactional
    public void deleteTestCaseById(UUID testCaseId) {
        // Find test case
        TestCase testCase = testCaseRepository.findById(testCaseId).orElseThrow(
                () -> new AppException(ErrorCode.TESTCASE_NOT_EXIST)
        );

        // Get associated problem
        Problem problem = testCase.getProblem();
        if (problem == null) {
            throw new AppException(ErrorCode.PROBLEM_NOT_EXIST);
        }

        // Remove test case from problem's test case list
        if (problem.getTestCases() != null) {
            problem.getTestCases().removeIf(tc -> tc.getTestcaseId().equals(testCaseId));
        }

        // Persist changes to problem (if needed)
        problemRepository.save(problem);

        // Delete testcase
        testCaseRepository.deleteById(testCaseId);

        // Optionally: delete saved file if stored
        TestCaseFileReader.deleteOneTestCaseFile(problem.getProblemName(), testCase.getOrder());
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

    public List<TestCaseCreationResponse> createMultipleTestCases(TestCaseMultipleCreationRequest request) {
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
                    .build();
            testCases.add(testCase);
        }

        if (problem.getTestCases() != null) {
            List<TestCase> newListTestCase = problem.getTestCases();
            newListTestCase.addAll(testCases);
            problem.setTestCases(newListTestCase);
        } else {
            problem.setTestCases(testCases);
        }

        testCaseRepository.saveAll(testCases);

        // Save test cases as .txt files
        TestCaseFileReader.saveTestCases(
                problem.getProblemName(),
                request.getInputs(),
                request.getOutputs()
        );

        return testCases.stream()
                .map(testCaseMapper::toTestCaseResponse)
                .collect(Collectors.toList());
    }

    public List<String> getSupportedDataTypes() {
        return SupportedDataType.getSupportedDataTypes();
    }

    public <T extends Number> List<T[]> generateNumericTestCases(TestCasesGenerationRequest<T> request) {
        NumberTestCaseGenerator<T> generator = new NumberTestCaseGenerator<>();
        return generator.generate1DTestCases(
                request.getNumberOfTestCases(),
                request.getMinArrayLength(),
                request.getMaxArrayLength(),
                request.getMinValue(),
                request.getMaxValue(),
                request.getDirectoryPath(),
                request.getClazz()
        );
    }
}