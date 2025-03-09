package com.example.problemservice.service;

import com.example.problemservice.dto.response.LeaderboardResponse;
import com.example.problemservice.dto.response.ProgressLanguageResponse;
import com.example.problemservice.dto.response.ProgressLevelResponse;
import com.example.problemservice.repository.ProblemRepository;
import com.example.problemservice.repository.ProblemSubmissionRepository;
import lombok.AllArgsConstructor;
import org.apache.catalina.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

        if (!results.isEmpty()) {
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

    public List<LeaderboardResponse> getProblemLeaderboard() {
        List<Object []> results = problemSubmissionRepository.getLeaderboard();
        return results.stream()
                .map(row -> {
                    String userId = String.valueOf((UUID) row[0]);
                    long totalScore = (long) row[1];
                    ProgressLevelResponse problemStat = getProgressLevel(UUID.fromString(userId));

                    return LeaderboardResponse.builder()
                            .point(totalScore)
                            .userId(userId)
                            .problemStat(LeaderboardResponse.ProblemStatResponse.builder()
                                    .easy(problemStat.getEasy().getSolved())
                                    .medium(problemStat.getMedium().getSolved())
                                    .hard(problemStat.getHard().getSolved())
                                    .total(
                                            problemStat.getEasy().getSolved() +
                                            problemStat.getMedium().getSolved() +
                                            problemStat.getHard().getSolved()
                                    )
                                    .build()
                            )
                            .build();
                })
                .toList();
    }

}
