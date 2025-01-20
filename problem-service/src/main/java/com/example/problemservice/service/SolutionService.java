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

        Problem problem = problemRepository.findById(
                     UUID.fromString(
                             request.getProblemId()
                     )
                ).orElseThrow(() -> new AppException(
                        ErrorCode.PROBLEM_NOT_EXIST)
                );

        Solution solution = solutionMapper.toSolution(request);

        solution.setProblem(problem);

        SolutionID solutionId = new SolutionID(
                UUID.fromString(
                        request.getProblemId()
                ),
                UUID.fromString(
                        request.getAuthorId()
                )
        );

        solution.setSolutionId(solutionId);

        solution = solutionRepository.save(solution);

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

    public List<DetailsSolutionResponse> getSolutionByProblemId(UUID problemId) {
        List<DetailsSolutionResponse> solutions = solutionRepository.findAllBySolutionId_ProblemId(problemId)
                .stream().map(solutionMapper::toDetailsSolutionResponse).toList();

        return solutions;
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
        SolutionIdRequest request = new SolutionIdRequest(
                problemId,
                authorId
        );

        SolutionID solutionId = solutionMapper.toSolutionID(request);
        solutionRepository.deleteById(solutionId);
    }


    
}