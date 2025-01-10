package com.example.problemservice.service;

import com.example.problemservice.dto.response.Problem.ProblemRowResponse;
import com.example.problemservice.exception.AppException;
import com.example.problemservice.exception.ErrorCode;
import com.example.problemservice.mapper.ProblemMapper;
import com.example.problemservice.model.Problem;
import com.example.problemservice.repository.ProblemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProblemService {
    private final ProblemRepository problemRepository;
    private final ProblemMapper problemMapper;

    public Problem createProblem(Problem problem) {
        return problemRepository.save(problem);
    }

    public Problem getProblem(UUID problemId) {
        return problemRepository.findById(problemId).orElseThrow(
                () -> new AppException(ErrorCode.PROBLEM_NOT_EXIST)
        );
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
        problemRepository.deleteById(problemId);
    }
}