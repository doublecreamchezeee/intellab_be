package com.example.problemservice.service;

import com.example.problemservice.exception.AppException;
import com.example.problemservice.exception.ErrorCode;
import com.example.problemservice.model.Problem;
import com.example.problemservice.repository.ProblemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProblemService {
    private final ProblemRepository problemRepository;

    public Problem createProblem(Problem problem) {
        return problemRepository.save(problem);
    }

    public Problem getProblem(UUID problemId) {
        return problemRepository.findById(problemId).orElseThrow(
                () -> new AppException(ErrorCode.PROBLEM_NOT_EXIST)
        );
    }
    public List<Problem> getAllProblems() {
        return problemRepository.findAll();
    }

    public void deleteProblem(UUID problemId) {
        problemRepository.deleteById(problemId);
    }
}