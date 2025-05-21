package com.example.problemservice.service;

import com.example.problemservice.dto.request.solution.SolutionCreationRequest;
import com.example.problemservice.dto.request.solution.SolutionIdRequest;
import com.example.problemservice.dto.request.solution.SolutionUpdateRequest;
import com.example.problemservice.dto.response.solution.DetailsSolutionResponse;
import com.example.problemservice.dto.response.solution.SolutionCreationResponse;
import com.example.problemservice.dto.response.solution.SolutionUpdateResponse;
import com.example.problemservice.exception.AppException;
import com.example.problemservice.exception.ErrorCode;
import com.example.problemservice.mapper.SolutionMapper;
import com.example.problemservice.model.Problem;
import com.example.problemservice.model.Solution;
import com.example.problemservice.model.composite.SolutionID;
import com.example.problemservice.repository.ProblemRepository;
import com.example.problemservice.model.composite.SolutionID;
import com.example.problemservice.repository.SolutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SolutionService {
    private final SolutionRepository solutionRepository;
    private final SolutionMapper solutionMapper;
    private final ProblemRepository problemRepository;

    public SolutionCreationResponse createSolution(SolutionCreationRequest request) {

        // Step 1: Fetch the related problem
        Problem problem = problemRepository.findById(
                UUID.fromString(request.getProblemId())
        ).orElseThrow(() -> new AppException(ErrorCode.PROBLEM_NOT_EXIST));

        // Step 2: Build composite key
        SolutionID solutionId = SolutionID.builder()
                .problemId(UUID.fromString(request.getProblemId()))
                .authorId(UUID.fromString(request.getAuthorId()))
                .build();

        // Step 3: Map request to entity and assign IDs + relationship
        Solution solution = solutionMapper.toSolution(request);
        solution.setId(solutionId);
        solution.setProblem(problem);

        // Step 4: Save to DB
        solution = solutionRepository.save(solution);

        problem.setCurrentCreationStep(5);
        problemRepository.save(problem);

        // Step 5: Return response
        return solutionMapper.toSolutionCreationResponse(solution);
    }



    public Solution getSolution(SolutionID solutionId) {
        return solutionRepository.findById(solutionId)
                .orElseThrow(() -> new AppException(
                        ErrorCode.SOLUTION_NOT_EXIST)
                );
    }

    public DetailsSolutionResponse getSolution(String problemId, String authorId) {
        SolutionID solutionId = new SolutionID(
                UUID.fromString(problemId),
                UUID.fromString(authorId)
        );

        Solution solution =  solutionRepository.findById(solutionId)
                .orElseThrow(() -> new AppException(
                        ErrorCode.SOLUTION_NOT_EXIST)
                );

        return solutionMapper.toDetailsSolutionResponse(solution);
    }

    public DetailsSolutionResponse getSolutionByProblemId(UUID problemId) {
        return solutionMapper.toDetailsSolutionResponse(solutionRepository.findByIdProblemId(problemId));
    }

    public SolutionUpdateResponse updateSolution(String problemId, String authorId, SolutionUpdateRequest request) {
        SolutionID solutionId = new SolutionID(
                UUID.fromString(problemId),
                UUID.fromString(authorId)
        );

        Solution solution = solutionRepository.findById(
                solutionId
        ).orElseThrow(() -> new AppException(
                ErrorCode.SOLUTION_NOT_EXIST)
        );

        solution.setContent(request.getContent());

        solution = solutionRepository.save(solution);

        return solutionMapper.toSolutionUpdateResponse(solution);

    }

    public void deleteSolution(String problemId, String authorId) {
        //solutionRepository.deleteById(UUID.fromString(problemId));
    }


    
}