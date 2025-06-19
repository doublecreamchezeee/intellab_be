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
import com.example.identityservice.model.User;
import com.example.identityservice.repository.LeaderboardRepository;
import com.example.identityservice.utility.ParseUUID;
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
    private final FirestoreService firestoreService;

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
        // Xác định offset và số lượng phần tử cần lấy từ danh sách responses
        // Nếu không có phần tử nào trong responses, ta cần thêm những người dùng có điểm số bằng 0
        int start = (int) pageable.getOffset();
        int end = start + pageable.getPageSize();

        // Nếu responses đủ, ta trả về trang với nội dung từ start đến end
        if (responses.size() > end)
        {
            List<LeaderboardResponse> pageContent = responses.subList(start, end);
            return new PageImpl<>(pageContent, pageable, responses.size());
        }
        // Nếu responses không đủ, ta cần thêm những người dùng có điểm số bằng 0
        return addZero(responses, start, end);
    }

    private Page<LeaderboardResponse> addZero(List<LeaderboardResponse> leaderboards, int start, int end) {
        List<String> uids = leaderboards.stream().map(LeaderboardResponse::getUserUid).toList();

        List<String> userUids = null;
        List<User> users = null;
        try {
            users = firestoreService.getAllUsers();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        users = users.stream().filter(user -> Objects.equals(user.getRole(), "user")).toList();

        userUids = new ArrayList<>(users.stream().map(User::getUid).toList());
        System.out.println(userUids);

        if (userUids == null || userUids.isEmpty()) {
            return null;
        }

        userUids.removeAll(uids);
        userUids.remove(null);
        userUids.remove("");
        int totalSize = leaderboards.size() + userUids.size();
        System.out.println("Total size: " + totalSize);
        System.out.println("Page size: " + (end - start));

        if(start > totalSize) {
            return new PageImpl<>(Collections.emptyList(), Pageable.ofSize(end - start), totalSize);
        }
        int startIndex = Math.max(leaderboards.size(), start);
        int endIndex = Math.min(totalSize, end);

        userUids = userUids.subList(startIndex, endIndex);


        List<LeaderboardResponse> zeroList =  userUids.stream().map(
                uid -> {

                    UserInfoResponse userInfoResponse = getUserInfoByUid(uid);

                    return LeaderboardResponse.builder()
                            .point(0L)
                            .displayName(userInfoResponse.getDisplayName())
                            .photoUrl(userInfoResponse.getPhotoUrl())
                            .firstName(userInfoResponse.getFirstName())
                            .lastName(userInfoResponse.getLastName())
                            .problemStat(null)
                            .courseStat(null)
                            .userUid(userInfoResponse.getUserId())
                            .build();
                }
        ).toList();

        if (leaderboards.size() >= start)
        {
            List<LeaderboardResponse> pageContent = new ArrayList<>(leaderboards.subList(start, Math.min(leaderboards.size(), end)));
            pageContent.addAll(zeroList);
            return new PageImpl<>(pageContent, Pageable.ofSize(end - start + 1), totalSize);
        } else {
            return new PageImpl<>(zeroList, Pageable.ofSize(end - start + 1), totalSize);
        }
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
                .userUid(user.getUserId())
                .build();
    }

    private UserInfoResponse getUserInfo(String userId) {
        try {
            return profileService.getUserInfo(firebaseAuthClient.getUid(UUID.fromString(userId)), "");
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private UserInfoResponse getUserInfoByUid(String userUid) {
        if (userUid == null || userUid.isEmpty()) {
            return UserInfoResponse.builder()
                    .displayName("Ambiguous User").build();
        }
        try{
            return profileService.getUserInfo(userUid, "");
        } catch (Exception e) {
            return UserInfoResponse.builder()
                    .displayName("Ambiguous User").build();
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

    public void updateLeaderboard(String userId, String type, Long additionalScore, ProblemStat problemStat, CourseStat courseStat) {
        UUID userUUID = UUID.fromString(userId);

        // Check if leaderboard entry exists for the given type
        Optional<Leaderboard> existingEntry = leaderboardRepository.findByUserIdAndType(userUUID, type);

        if (existingEntry.isPresent()) {
            // Update existing entry
            Leaderboard leaderboard = existingEntry.get();
            Long oldScore = leaderboard.getScore();
            leaderboard.setScore(oldScore + additionalScore);

            if ("problem".equals(type)) {
                ProblemStat temp = leaderboard.getProblemStat();
                temp.setEasy(temp.getEasy() + problemStat.getEasy());
                temp.setMedium(temp.getMedium() + problemStat.getMedium());
                temp.setHard(temp.getHard() + problemStat.getHard());

                temp.setTotalProblem(temp.getEasy() + temp.getMedium() + temp.getHard());

                leaderboard.setProblemStat(temp);
            } else if ("course".equals(type)) {
                CourseStat temp = leaderboard.getCourseStat();

                temp.setBeginner(temp.getBeginner() + courseStat.getBeginner());
                temp.setIntermediate(temp.getIntermediate() + courseStat.getIntermediate());
                temp.setAdvanced(temp.getAdvanced() + courseStat.getAdvanced());

                temp.setTotalCourse(temp.getAdvanced() + temp.getBeginner() + temp.getIntermediate());
                leaderboard.setCourseStat(temp);
            }

            leaderboardRepository.save(leaderboard);
        } else {
            // Create a new leaderboard entry
            Leaderboard newLeaderboard = Leaderboard.builder()
                    .userId(userUUID)
                    .type(type)
                    .score(additionalScore)
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

    public Integer getMyPoint(String userId) {
        UUID userUUID = ParseUUID.normalizeUID(userId);

        Optional<Leaderboard> leaderboard = leaderboardRepository.findByUserIdAndType(userUUID, "all");

        // Return 0 if no leaderboard entry exists for the user
        return leaderboard.map(value -> value.getScore().intValue()).orElse(0);
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