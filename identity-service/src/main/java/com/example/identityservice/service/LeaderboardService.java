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
import com.example.identityservice.model.CourseStat;
import com.example.identityservice.model.Leaderboard;
import com.example.identityservice.model.ProblemStat;
import com.example.identityservice.repository.LeaderboardRepository;
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
    private final LeaderboardRepository leaderboardRepository;
    public Page<LeaderboardResponse> getLeaderboard(Pageable pageable, String filter) {
        List<Leaderboard> leaderboards;

        if (filter.equals("problem")) {
            leaderboards = leaderboardRepository.findByTypeOrderByScoreDesc("problem", pageable);
        } else if (filter.equals("course")) {
            leaderboards = leaderboardRepository.findByTypeOrderByScoreDesc("course", pageable);
        } else {
            leaderboards = leaderboardRepository.findByTypeOrderByScoreDesc("all", pageable);
        }

        List<LeaderboardResponse> responses = leaderboards.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(responses, pageable, responses.size());

    }

    private LeaderboardResponse convertToResponse(Leaderboard leaderboard) {
        UserInfoResponse user = getUserInfo(String.valueOf(leaderboard.getUserId()));

        return LeaderboardResponse.builder()
                .point(leaderboard.getScore())
                .displayName(user.getDisplayName())
                .photoUrl(user.getPhotoUrl())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .problemStat("problem".equals(leaderboard.getType()) ? mapProblemStat(leaderboard.getProblemStat()) : null)
                .courseStat("course".equals(leaderboard.getType()) ? mapCourseStat(leaderboard.getCourseStat()) : null)
                .build();
    }

    private UserInfoResponse getUserInfo(String userId) {
        try {
            return profileService.getUserInfo(firebaseAuthClient.getUid(UUID.fromString(userId)), "");
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private LeaderboardResponse.ProblemStatResponse mapProblemStat(ProblemStat problemStat) {
        return problemStat == null ? null : new LeaderboardResponse.ProblemStatResponse(
                problemStat.getEasy(),
                problemStat.getMedium(),
                problemStat.getHard(),
                problemStat.getTotalProblem()
        );
    }

    private LeaderboardResponse.CourseStatResponse mapCourseStat(CourseStat courseStat) {
        return courseStat == null ? null : new LeaderboardResponse.CourseStatResponse(
                courseStat.getBeginner(),
                courseStat.getIntermediate(),
                courseStat.getAdvanced(),
                courseStat.getTotalCourse()
        );
    }

    public void updateLeaderboard(String userId, String type, Long newScore, ProblemStat problemStat, CourseStat courseStat) {
        UUID userUUID = UUID.fromString(userId);

        // Check if leaderboard entry exists for the given type
        Optional<Leaderboard> existingEntry = leaderboardRepository.findByUserIdAndType(userUUID, type);

        if (existingEntry.isPresent()) {
            // Update existing entry
            Leaderboard leaderboard = existingEntry.get();
            leaderboard.setScore(newScore);

            if ("problem".equals(type)) {
                problemStat.setTotalProblem(problemStat.getEasy() + problemStat.getMedium() + problemStat.getHard());
                leaderboard.setProblemStat(problemStat);
            } else if ("course".equals(type)) {
                courseStat.setTotalCourse(courseStat.getAdvanced() + courseStat.getBeginner() + courseStat.getIntermediate());
                leaderboard.setCourseStat(courseStat);
            }

            leaderboardRepository.save(leaderboard);
        } else {
            // Create a new leaderboard entry
            Leaderboard newLeaderboard = Leaderboard.builder()
                    .userId(userUUID)
                    .type(type)
                    .score(newScore)
                    .problemStat("problem".equals(type) ? problemStat : null)
                    .courseStat("course".equals(type) ? courseStat : null)
                    .build();

            leaderboardRepository.save(newLeaderboard);
        }

        // 🔥 Automatically update "all" type leaderboard
        updateMergedLeaderboard(userUUID);
    }

    private void updateMergedLeaderboard(UUID userId) {
        // Get existing problem and course scores
        Optional<Leaderboard> problemEntry = leaderboardRepository.findByUserIdAndType(userId, "problem");
        Optional<Leaderboard> courseEntry = leaderboardRepository.findByUserIdAndType(userId, "course");

        Long problemScore = problemEntry.map(Leaderboard::getScore).orElse(0L);
        Long courseScore = courseEntry.map(Leaderboard::getScore).orElse(0L);

        Long totalScore = problemScore + courseScore;

        ProblemStat problemStat = problemEntry.map(Leaderboard::getProblemStat).orElse(null);
        CourseStat courseStat = courseEntry.map(Leaderboard::getCourseStat).orElse(null);

        // Check if "all" leaderboard entry exists
        Optional<Leaderboard> allEntry = leaderboardRepository.findByUserIdAndType(userId, "all");

        if (allEntry.isPresent()) {
            // Update existing "all" entry
            Leaderboard leaderboard = allEntry.get();
            leaderboard.setScore(totalScore);
            leaderboard.setProblemStat(problemStat);

            leaderboard.setCourseStat(courseStat);
            leaderboardRepository.save(leaderboard);
        } else {
            // Create a new "all" leaderboard entry
            Leaderboard newLeaderboard = Leaderboard.builder()
                    .userId(userId)
                    .type("all")
                    .score(totalScore)
                    .problemStat(problemStat)
                    .courseStat(courseStat)
                    .build();

            leaderboardRepository.save(newLeaderboard);
        }
    }

//    private Page<LeaderboardResponse> getProblemLeaderboard(Pageable pageable) {
//        List<LeaderboardProblemResponse> problemResponses = problemClient.getLeaderboard().block();
//
//        List<LeaderboardResponse> leaderboardResponses = problemResponses.stream()
//                .map(problemResponse -> {
//                    UserInfoResponse userInfo = getUserInfo(problemResponse.getUserId());
//                    if (userInfo == null) throw new AppException(ErrorCode.USER_NOT_EXISTED);
//
//                    return LeaderboardResponse.builder()
//                            .point(problemResponse.getPoint())
//                            .displayName(userInfo.getDisplayName())
//                            .firstName(userInfo.getFirstName())
//                            .lastName(userInfo.getLastName())
//                            .photoUrl(userInfo.getPhotoUrl())
//                            .problemStat(LeaderboardResponse.ProblemStatResponse.builder()
//                                    .easy(problemResponse.getProblemStat().getEasy())
//                                    .medium(problemResponse.getProblemStat().getMedium())
//                                    .hard(problemResponse.getProblemStat().getHard())
//                                    .total(problemResponse.getProblemStat().getTotal())
//                                    .build())
//                            .build();
//                }).toList();
//
//        return paginateResults(leaderboardResponses, pageable);
//    }
//
//    private Page<LeaderboardResponse> getCourseLeaderboard(Pageable pageable) {
//        List<LeaderboardCourseResponse> courseResponses = courseClient.getLeaderboard().block();
//
//        List<LeaderboardResponse> leaderboardResponses = courseResponses.stream()
//                .map(courseResponse -> {
//                    UserInfoResponse userInfo = getUserInfo(courseResponse.getUserId());
//                    if (userInfo == null) throw new AppException(ErrorCode.USER_NOT_EXISTED);
//
//                    return LeaderboardResponse.builder()
//                            .point(courseResponse.getPoint())
//                            .displayName(userInfo.getDisplayName())
//                            .firstName(userInfo.getFirstName())
//                            .lastName(userInfo.getLastName())
//                            .photoUrl(userInfo.getPhotoUrl())
//                            .courseStat(LeaderboardResponse.CourseStatResponse.builder()
//                                    .beginner(courseResponse.getCourseStat().getBeginner())
//                                    .intermediate(courseResponse.getCourseStat().getIntermediate())
//                                    .advanced(courseResponse.getCourseStat().getAdvanced())
//                                    .total(courseResponse.getCourseStat().getTotal())
//                                    .build())
//                            .build();
//                }).toList();
//
//        return paginateResults(leaderboardResponses, pageable);
//    }
//
//    private Page<LeaderboardResponse> getMergedLeaderboard(Pageable pageable) {
//        List<LeaderboardProblemResponse> problemResponses = problemClient.getLeaderboard().block();
//        List<LeaderboardCourseResponse> courseResponses = courseClient.getLeaderboard().block();
//
//        Map<String, LeaderboardResponse> mergedLeaderboard = new HashMap<>();
//
//        for (LeaderboardProblemResponse problemResponse : problemResponses) {
//            UserInfoResponse userInfo = getUserInfo(problemResponse.getUserId());
//            if (userInfo == null) throw new AppException(ErrorCode.USER_NOT_EXISTED);
//
//            mergedLeaderboard.put(problemResponse.getUserId(), LeaderboardResponse.builder()
//                    .point(problemResponse.getPoint())
//                    .displayName(userInfo.getDisplayName())
//                    .firstName(userInfo.getFirstName())
//                    .lastName(userInfo.getLastName())
//                    .photoUrl(userInfo.getPhotoUrl())
//                    .problemStat(LeaderboardResponse.ProblemStatResponse.builder()
//                            .easy(problemResponse.getProblemStat().getEasy())
//                            .medium(problemResponse.getProblemStat().getMedium())
//                            .hard(problemResponse.getProblemStat().getHard())
//                            .total(problemResponse.getProblemStat().getTotal())
//                            .build())
//                    .build());
//        }
//
//        for (LeaderboardCourseResponse courseResponse : courseResponses) {
//            UserInfoResponse userInfo = getUserInfo(courseResponse.getUserId());
//            if (userInfo == null) throw new AppException(ErrorCode.USER_NOT_EXISTED);
//
//            mergedLeaderboard.merge(courseResponse.getUserId(), LeaderboardResponse.builder()
//                    .point(courseResponse.getPoint())
//                    .displayName(userInfo.getDisplayName())
//                    .firstName(userInfo.getFirstName())
//                    .lastName(userInfo.getLastName())
//                    .photoUrl(userInfo.getPhotoUrl())
//                    .courseStat(LeaderboardResponse.CourseStatResponse.builder()
//                            .beginner(courseResponse.getCourseStat().getBeginner())
//                            .intermediate(courseResponse.getCourseStat().getIntermediate())
//                            .advanced(courseResponse.getCourseStat().getAdvanced())
//                            .total(courseResponse.getCourseStat().getTotal())
//                            .build())
//                    .build(), (existing, newEntry) -> {
//                return LeaderboardResponse.builder()
//                        .point(existing.getPoint() + newEntry.getPoint())
//                        .displayName(existing.getDisplayName())
//                        .firstName(existing.getFirstName())
//                        .lastName(existing.getLastName())
//                        .photoUrl(existing.getPhotoUrl())
//                        .problemStat(existing.getProblemStat() != null ? existing.getProblemStat() : newEntry.getProblemStat())
//                        .courseStat(existing.getCourseStat() != null ? existing.getCourseStat() : newEntry.getCourseStat())
//                        .build();
//            });
//        }
//
//        List<LeaderboardResponse> leaderboardResponses = mergedLeaderboard.values()
//                .stream()
//                .sorted(Comparator.comparingLong(LeaderboardResponse::getPoint).reversed())
//                .collect(Collectors.toList());
//
//        return paginateResults(leaderboardResponses, pageable);
//    }

    private Page<LeaderboardResponse> paginateResults(List<LeaderboardResponse> leaderboardResponses, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), leaderboardResponses.size());
        List<LeaderboardResponse> pageContent = leaderboardResponses.subList(start, end);
        return new PageImpl<>(pageContent, pageable, leaderboardResponses.size());
    }
}