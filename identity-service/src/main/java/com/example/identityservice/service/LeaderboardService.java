package com.example.identityservice.service;

import com.example.identityservice.client.FirebaseAuthClient;
import com.example.identityservice.client.ProblemClient;
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

import java.util.List;

@Service
@AllArgsConstructor
public class LeaderboardService {
    private final ProblemClient problemClient;
    private final FirebaseAuthClient firebaseAuthClient;

    public Page<LeaderboardResponse> getLeaderboard(Pageable pageable, String filter) {

        if (filter.equals("problem"))
        {
            List<LeaderboardProblemResponse> problemResponses = problemClient.getLeaderboard().block();

            List<LeaderboardResponse> leaderboardResponses = problemResponses.stream()
                    .map(problemResponse -> {
                        String userId = problemResponse.getUserId();
                        UserInfoResponse userInfo = firebaseAuthClient.getUserInfo(userId, "");
                        if (userInfo == null)  throw new AppException(ErrorCode.USER_NOT_EXISTED);
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
                                        .build()
                                )
                                .build();
                    }).toList();

            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), leaderboardResponses.size());
            List<LeaderboardResponse> pageContent = leaderboardResponses.subList(start, end);

            return new PageImpl<>(pageContent, pageable, leaderboardResponses.size());
        }
        return Page.empty();
    }
}
