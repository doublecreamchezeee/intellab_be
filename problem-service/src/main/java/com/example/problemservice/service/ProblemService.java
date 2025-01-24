package com.example.problemservice.service;

import com.example.problemservice.converter.ProblemStructureConverter;
import com.example.problemservice.core.ProblemStructure;
import com.example.problemservice.dto.request.problem.ProblemCreationRequest;
import com.example.problemservice.dto.response.Problem.ProblemCreationResponse;
import com.example.problemservice.dto.response.Problem.ProblemRowResponse;
import com.example.problemservice.exception.AppException;
import com.example.problemservice.exception.ErrorCode;
import com.example.problemservice.mapper.ProblemMapper;
import com.example.problemservice.model.Problem;
import com.example.problemservice.repository.ProblemRepository;
import com.example.problemservice.utils.MarkdownUtility;
import com.example.problemservice.utils.TestCaseFileReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProblemService {
    private final ProblemRepository problemRepository;
    private final ProblemMapper problemMapper;

    public ProblemCreationResponse createProblem(ProblemCreationRequest request) {
        Problem problem = problemMapper.toProblem(request);

        problem.setProblemStructure(
                ProblemStructureConverter.convertObjectToString(
                        request.getProblemStructure()
                )
        );

        Problem savedProblem =  problemRepository.save(problem);

        MarkdownUtility.saveProblemAsMarkdown(savedProblem);

        //return response
        ProblemCreationResponse response = problemMapper
                .toProblemCreationResponse(savedProblem);

        response.setProblemStructure(
                request.getProblemStructure()
        );

        return response;
    }

    public Problem getProblem(UUID problemId) {
        return problemRepository.findById(problemId).orElseThrow(
                () -> new AppException(ErrorCode.PROBLEM_NOT_EXIST)
        );
    }
    
    public List<Problem> getAllProblems() {
        return problemRepository.findAll();
    }

    public Page<ProblemRowResponse> searchProblems(Pageable pageable, String keyword) {
        Page<Problem> problems = problemRepository.findAllByProblemNameContainingIgnoreCase(keyword, pageable);
        return problems.map(problemMapper::toProblemRowResponse);
    }

    public Page<ProblemRowResponse> getAllProblems(String category, Pageable pageable) {
        Page<Problem> problems = problemRepository.findAll(pageable);
        return problems.map(problemMapper::toProblemRowResponse);
    }

    public void deleteProblem(UUID problemId) {
        Problem problem = problemRepository.findById(problemId).orElseThrow(
                () -> new AppException(ErrorCode.PROBLEM_NOT_EXIST)
        );
        MarkdownUtility.deleteProblemFolder(problem.getProblemName());
        problemRepository.deleteById(problemId);
    }

    public ProblemCreationResponse updateProblem(UUID problemId, ProblemCreationRequest request) {
        Problem existingProblem = problemRepository.findById(problemId).orElseThrow(
                () -> new AppException(ErrorCode.PROBLEM_NOT_EXIST)
        );

        String problemStructure = ProblemStructureConverter.convertObjectToString(
                request.getProblemStructure()
        );

        existingProblem.setProblemStructure(problemStructure);

        problemMapper.updateProblemFromRequest(request, existingProblem);
        Problem updatedProblem = problemRepository.save(existingProblem);

        // Update the Markdown files
        MarkdownUtility.saveProblemAsMarkdown(updatedProblem);

        return problemMapper.toProblemCreationResponse(updatedProblem);
    }

    public void getProblemById(UUID problemId) {
        Problem problem =  problemRepository.findById(problemId).orElseThrow(
                () -> new AppException(ErrorCode.PROBLEM_NOT_EXIST)
        );

        String problemContent = MarkdownUtility.readMarkdownFromFile(
                problem.getProblemName(), "Problem.md");

        List<String> inputs = TestCaseFileReader.getProblemTestCases(
                problem.getProblemName(), TestCaseFileReader.INPUT);

        List<String> outputs = TestCaseFileReader.getProblemTestCases(
                problem.getProblemName(), TestCaseFileReader.OUTPUT);

        log.info("Problem Content: {}", problemContent);
        log.info("Inputs: {}", inputs);
        log.info("Outputs: {}", outputs);

        return;
        
    }
}