package com.example.problemservice.service;

import com.example.problemservice.dto.response.ProgressLanguageResponse;
import com.example.problemservice.dto.response.ProgressLevelResponse;
import com.example.problemservice.repository.ProblemRepository;
import com.example.problemservice.repository.ProblemSubmissionRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class StatisticsService {
    private final ProblemRepository problemRepository;
    private final ProblemSubmissionRepository problemSubmissionRepository;

    public ProgressLevelResponse getProgressLevel(UUID userId) {
        int totalProblems = (int) problemRepository.count();
        System.out.println("check" + userId);

        // Count problems solved by the user for each level
        long easySolved = problemSubmissionRepository.countSolvedProblemsByLevelAndUser("easy", userId);
        long mediumSolved = problemSubmissionRepository.countSolvedProblemsByLevelAndUser("medium", userId);
        long hardSolved = problemSubmissionRepository.countSolvedProblemsByLevelAndUser("hard", userId);

        // Count total problems for each level
        long easyTotal = problemRepository.countByProblemLevel("easy");
        long mediumTotal = problemRepository.countByProblemLevel("medium");
        long hardTotal = problemRepository.countByProblemLevel("hard");

        // Build the response
        return ProgressLevelResponse.builder()
                .totalProblems(totalProblems)
                .easy(ProgressLevelResponse.DifficultyStatistics.builder()
                        .solved((int) easySolved)
                        .max((int) easyTotal)
                        .build())
                .medium(ProgressLevelResponse.DifficultyStatistics.builder()
                        .solved((int) mediumSolved)
                        .max((int) mediumTotal)
                        .build())
                .hard(ProgressLevelResponse.DifficultyStatistics.builder()
                        .solved((int) hardSolved)
                        .max((int) hardTotal)
                        .build())
                .build();
    }

    public ProgressLanguageResponse getProgressLanguage(UUID userId) {
        System.out.println("check " + userId);

        List<Object[]> results = problemSubmissionRepository.findTop3LanguagesBySolvedCount(userId);

        ProgressLanguageResponse.LanguageStatistics top1 = null;
        ProgressLanguageResponse.LanguageStatistics top2 = null;
        ProgressLanguageResponse.LanguageStatistics top3 = null;

        if (results.size() > 0) {
            top1 = new ProgressLanguageResponse.LanguageStatistics(
                    ((Long) results.get(0)[1]).intValue(),
                    (String) results.get(0)[0]
            );
        }
        if (results.size() > 1) {
            top2 = new ProgressLanguageResponse.LanguageStatistics(
                    ((Long) results.get(1)[1]).intValue(),
                    (String) results.get(1)[0]
            );
        }
        if (results.size() > 2) {
            top3 = new ProgressLanguageResponse.LanguageStatistics(
                    ((Long) results.get(2)[1]).intValue(),
                    (String) results.get(2)[0]
            );
        }

        // Trả về kết quả
        return new ProgressLanguageResponse(top1, top2, top3);
    }
}
