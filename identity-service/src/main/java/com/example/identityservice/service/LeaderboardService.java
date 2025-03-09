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
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class LeaderboardService {
    private final ProblemClient problemClient;
    private final CourseClient courseClient;
    private final ProfileService profileService;
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

    private UserInfoResponse getUserInfo(String userId) {
        try {
            return profileService.getUserInfo(firebaseAuthClient.getUid(UUID.fromString(userId)), "");
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private Page<LeaderboardResponse> getProblemLeaderboard(Pageable pageable) {
        List<LeaderboardProblemResponse> problemResponses = problemClient.getLeaderboard().block();

        List<LeaderboardResponse> leaderboardResponses = problemResponses.stream()
                .map(problemResponse -> {
                    UserInfoResponse userInfo = getUserInfo(problemResponse.getUserId());
                    if (userInfo == null) throw new AppException(ErrorCode.USER_NOT_EXISTED);

                    return LeaderboardResponse.builder()
                            .point(problemResponse.getPoint())
                            .displayName(userInfo.getDisplayName())
                            .firstName(userInfo.getFirstName())
                            .lastName(userInfo.getLastName())
                            .photoUrl(userInfo.getPhotoUrl())
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
                    UserInfoResponse userInfo = getUserInfo(courseResponse.getUserId());
                    if (userInfo == null) throw new AppException(ErrorCode.USER_NOT_EXISTED);

                    return LeaderboardResponse.builder()
                            .point(courseResponse.getPoint())
                            .displayName(userInfo.getDisplayName())
                            .firstName(userInfo.getFirstName())
                            .lastName(userInfo.getLastName())
                            .photoUrl(userInfo.getPhotoUrl())
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

        for (LeaderboardProblemResponse problemResponse : problemResponses) {
            UserInfoResponse userInfo = getUserInfo(problemResponse.getUserId());
            if (userInfo == null) throw new AppException(ErrorCode.USER_NOT_EXISTED);

            mergedLeaderboard.put(problemResponse.getUserId(), LeaderboardResponse.builder()
                    .point(problemResponse.getPoint())
                    .displayName(userInfo.getDisplayName())
                    .firstName(userInfo.getFirstName())
                    .lastName(userInfo.getLastName())
                    .photoUrl(userInfo.getPhotoUrl())
                    .problemStat(LeaderboardResponse.ProblemStatResponse.builder()
                            .easy(problemResponse.getProblemStat().getEasy())
                            .medium(problemResponse.getProblemStat().getMedium())
                            .hard(problemResponse.getProblemStat().getHard())
                            .total(problemResponse.getProblemStat().getTotal())
                            .build())
                    .build());
        }

        for (LeaderboardCourseResponse courseResponse : courseResponses) {
            UserInfoResponse userInfo = getUserInfo(courseResponse.getUserId());
            if (userInfo == null) throw new AppException(ErrorCode.USER_NOT_EXISTED);

            mergedLeaderboard.merge(courseResponse.getUserId(), LeaderboardResponse.builder()
                    .point(courseResponse.getPoint())
                    .displayName(userInfo.getDisplayName())
                    .firstName(userInfo.getFirstName())
                    .lastName(userInfo.getLastName())
                    .photoUrl(userInfo.getPhotoUrl())
                    .courseStat(LeaderboardResponse.CourseStatResponse.builder()
                            .beginner(courseResponse.getCourseStat().getBeginner())
                            .intermediate(courseResponse.getCourseStat().getIntermediate())
                            .advanced(courseResponse.getCourseStat().getAdvanced())
                            .total(courseResponse.getCourseStat().getTotal())
                            .build())
                    .build(), (existing, newEntry) -> {
                return LeaderboardResponse.builder()
                        .point(existing.getPoint() + newEntry.getPoint())
                        .displayName(existing.getDisplayName())
                        .firstName(existing.getFirstName())
                        .lastName(existing.getLastName())
                        .photoUrl(existing.getPhotoUrl())
                        .problemStat(existing.getProblemStat() != null ? existing.getProblemStat() : newEntry.getProblemStat())
                        .courseStat(existing.getCourseStat() != null ? existing.getCourseStat() : newEntry.getCourseStat())
                        .build();
            });
        }

        List<LeaderboardResponse> leaderboardResponses = mergedLeaderboard.values()
                .stream()
                .sorted(Comparator.comparingLong(LeaderboardResponse::getPoint).reversed())
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