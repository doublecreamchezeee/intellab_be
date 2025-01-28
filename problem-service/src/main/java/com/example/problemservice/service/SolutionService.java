package com.example.problemservice.service;

import com.example.problemservice.exception.AppException;
import com.example.problemservice.exception.ErrorCode;
import com.example.problemservice.model.Solution;
import com.example.problemservice.model.composite.SolutionID;
import com.example.problemservice.repository.SolutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SolutionService {
    private final SolutionRepository solutionRepository;

    public Solution createSolution(Solution solution) {
        return solutionRepository.save(solution);
    }

    public Solution getSolution(SolutionID solutionId) {
        return solutionRepository.findById(solutionId)
                .orElseThrow(() -> new AppException(
                        ErrorCode.SOLUTION_NOT_EXIST)
                );
    }

    
}