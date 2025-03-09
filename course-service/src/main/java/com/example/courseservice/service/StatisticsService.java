package com.example.courseservice.service;

import com.example.courseservice.dto.response.LeaderboardResponse;
import com.example.courseservice.repository.CourseRepository;
import com.example.courseservice.repository.UserCoursesRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class StatisticsService {
    private final UserCoursesRepository userCoursesRepository;

    public List<LeaderboardResponse> getCourseLeaderboard() {
        List<Object[]> results = userCoursesRepository.getLeaderboard();

        return results.stream()
                .map(row -> {
                    String userId = String.valueOf((UUID) row[0]);
                    long totalScore = ((Number) row[1]).longValue();
                    int beginner = ((Number) row[2]).intValue();
                    int intermediate = ((Number) row[3]).intValue();
                    int advanced = ((Number) row[4]).intValue();
                    int total = beginner + intermediate + advanced;

                    return LeaderboardResponse.builder()
                            .point(totalScore)
                            .userId(userId)
                            .courseStat(LeaderboardResponse.CourseStatResponse.builder()
                                    .beginner(beginner)
                                    .intermediate(intermediate)
                                    .advanced(advanced)
                                    .total(total)
                                    .build())
                            .build();
                })
                .toList();
    }
}
