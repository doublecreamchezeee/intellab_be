package com.example.identityservice.service;

import com.example.identityservice.client.CourseClient;
import com.example.identityservice.client.FirebaseAuthClient;
import com.example.identityservice.client.ProblemClient;
import com.example.identityservice.dto.response.LeaderboardCourseResponse;
import com.example.identityservice.dto.response.LeaderboardProblemResponse;
import com.example.identityservice.dto.response.LeaderboardResponse;
import com.example.identityservice.dto.response.auth.UserInfoResponse;
import com.example.identityservice.exception.AppException;
import com.example.identityservice.exception.ErrorCode;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class LeaderboardService {
    private final ProblemClient problemClient;
    private final CourseClient courseClient;
    private final FirebaseAuthClient firebaseAuthClient;

    public Page<LeaderboardResponse> getLeaderboard(Pageable pageable, String filter) {
        if (filter.equals("problem")) {
            return getProblemLeaderboard(pageable);
        } else if (filter.equals("course")) {
            return getCourseLeaderboard(pageable);
        } else {
            return getMergedLeaderboard(pageable);
        }
    }

    private Page<LeaderboardResponse> getProblemLeaderboard(Pageable pageable) {
        List<LeaderboardProblemResponse> problemResponses = problemClient.getLeaderboard().block();

        List<LeaderboardResponse> leaderboardResponses = problemResponses.stream()
                .map(problemResponse -> {
                    String userId = problemResponse.getUserId();
                    UserInfoResponse userInfo = firebaseAuthClient.getUserInfo(userId, "");
                    if (userInfo == null) throw new AppException(ErrorCode.USER_NOT_EXISTED);

                    return LeaderboardResponse.builder()
                            .point(problemResponse.getPoint())
                            .displayName(userInfo.getDisplayName())
                            .firstName(userInfo.getFirstName())
                            .lastName(userInfo.getLastName())
                            .problemStat(LeaderboardResponse.ProblemStatResponse.builder()
                                    .easy(problemResponse.getProblemStat().getEasy())
                                    .medium(problemResponse.getProblemStat().getMedium())
                                    .hard(problemResponse.getProblemStat().getHard())
                                    .total(problemResponse.getProblemStat().getTotal())
                                    .build())
                            .build();
                }).toList();

        return paginateResults(leaderboardResponses, pageable);
    }

    private Page<LeaderboardResponse> getCourseLeaderboard(Pageable pageable) {
        List<LeaderboardCourseResponse> courseResponses = courseClient.getLeaderboard().block();

        List<LeaderboardResponse> leaderboardResponses = courseResponses.stream()
                .map(courseResponse -> {
                    String userId = courseResponse.getUserId();
                    UserInfoResponse userInfo = firebaseAuthClient.getUserInfo(userId, "");
                    if (userInfo == null) throw new AppException(ErrorCode.USER_NOT_EXISTED);

                    return LeaderboardResponse.builder()
                            .point(courseResponse.getPoint())
                            .displayName(userInfo.getDisplayName())
                            .firstName(userInfo.getFirstName())
                            .lastName(userInfo.getLastName())
                            .courseStat(LeaderboardResponse.CourseStatResponse.builder()
                                    .beginner(courseResponse.getCourseStat().getBeginner())
                                    .intermediate(courseResponse.getCourseStat().getIntermediate())
                                    .advanced(courseResponse.getCourseStat().getAdvanced())
                                    .total(courseResponse.getCourseStat().getTotal())
                                    .build())
                            .build();
                }).toList();

        return paginateResults(leaderboardResponses, pageable);
    }

    private Page<LeaderboardResponse> getMergedLeaderboard(Pageable pageable) {
        List<LeaderboardProblemResponse> problemResponses = problemClient.getLeaderboard().block();
        List<LeaderboardCourseResponse> courseResponses = courseClient.getLeaderboard().block();

        Map<String, LeaderboardResponse> mergedLeaderboard = new HashMap<>();

        // Process problem leaderboard
        for (LeaderboardProblemResponse problemResponse : problemResponses) {
            String userId = problemResponse.getUserId();
            UserInfoResponse userInfo = firebaseAuthClient.getUserInfo(userId, "");
            if (userInfo == null) throw new AppException(ErrorCode.USER_NOT_EXISTED);

            mergedLeaderboard.put(userId, LeaderboardResponse.builder()
                    .point(problemResponse.getPoint())
                    .displayName(userInfo.getDisplayName())
                    .firstName(userInfo.getFirstName())
                    .lastName(userInfo.getLastName())
                    .problemStat(LeaderboardResponse.ProblemStatResponse.builder()
                            .easy(problemResponse.getProblemStat().getEasy())
                            .medium(problemResponse.getProblemStat().getMedium())
                            .hard(problemResponse.getProblemStat().getHard())
                            .total(problemResponse.getProblemStat().getTotal())
                            .build())
                    .build());
        }

        // Process course leaderboard
        for (LeaderboardCourseResponse courseResponse : courseResponses) {
            String userId = courseResponse.getUserId();
            UserInfoResponse userInfo = firebaseAuthClient.getUserInfo(userId, "");
            if (userInfo == null) throw new AppException(ErrorCode.USER_NOT_EXISTED);

            mergedLeaderboard.merge(userId, LeaderboardResponse.builder()
                    .point(courseResponse.getPoint())
                    .displayName(userInfo.getDisplayName())
                    .firstName(userInfo.getFirstName())
                    .lastName(userInfo.getLastName())
                    .courseStat(LeaderboardResponse.CourseStatResponse.builder()
                            .beginner(courseResponse.getCourseStat().getBeginner())
                            .intermediate(courseResponse.getCourseStat().getIntermediate())
                            .advanced(courseResponse.getCourseStat().getAdvanced())
                            .total(courseResponse.getCourseStat().getTotal())
                            .build())
                    .build(), (existing, newEntry) -> {
                return LeaderboardResponse.builder()
                        .point(existing.getPoint() + newEntry.getPoint()) // Sum points
                        .displayName(existing.getDisplayName())
                        .firstName(existing.getFirstName())
                        .lastName(existing.getLastName())
                        .problemStat(existing.getProblemStat() != null ? existing.getProblemStat() : newEntry.getProblemStat())
                        .courseStat(existing.getCourseStat() != null ? existing.getCourseStat() : newEntry.getCourseStat())
                        .build();
            });
        }

        // Convert map to sorted list
        List<LeaderboardResponse> leaderboardResponses = mergedLeaderboard.values()
                .stream()
                .sorted(Comparator.comparingLong(LeaderboardResponse::getPoint).reversed()) // Sort by total points
                .collect(Collectors.toList());

        return paginateResults(leaderboardResponses, pageable);
    }

    private Page<LeaderboardResponse> paginateResults(List<LeaderboardResponse> leaderboardResponses, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), leaderboardResponses.size());
        List<LeaderboardResponse> pageContent = leaderboardResponses.subList(start, end);
        return new PageImpl<>(pageContent, pageable, leaderboardResponses.size());
    }
}
