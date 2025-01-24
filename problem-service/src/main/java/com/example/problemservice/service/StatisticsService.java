package com.example.problemservice.service;

import com.example.problemservice.dto.response.ProgressResponse;
import com.example.problemservice.repository.ProblemRepository;
import com.example.problemservice.repository.ProblemSubmissionRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@AllArgsConstructor
public class StatisticsService {
    private final ProblemRepository problemRepository;
    private final ProblemSubmissionRepository problemSubmissionRepository;

    public ProgressResponse getProgress(UUID userId) {
        int totalProblems = (int) problemRepository.count();

        // Count problems solved by the user for each level
        long easySolved = problemSubmissionRepository.countSolvedProblemsByLevelAndUser("EASY", userId);
        long mediumSolved = problemSubmissionRepository.countSolvedProblemsByLevelAndUser("MEDIUM", userId);
        long hardSolved = problemSubmissionRepository.countSolvedProblemsByLevelAndUser("HARD", userId);

        // Count total problems for each level
        long easyTotal = problemRepository.countByProblemLevel("easy");
        long mediumTotal = problemRepository.countByProblemLevel("medium");
        long hardTotal = problemRepository.countByProblemLevel("hard");

        // Build the response
        return ProgressResponse.builder()
                .totalProblems(totalProblems)
                .easy(ProgressResponse.DifficultyStatistics.builder()
                        .solved((int) easySolved)
                        .max((int) easyTotal)
                        .build())
                .medium(ProgressResponse.DifficultyStatistics.builder()
                        .solved((int) mediumSolved)
                        .max((int) mediumTotal)
                        .build())
                .hard(ProgressResponse.DifficultyStatistics.builder()
                        .solved((int) hardSolved)
                        .max((int) hardTotal)
                        .build())
                .build();
    }
}
