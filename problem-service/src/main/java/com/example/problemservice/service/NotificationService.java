package com.example.problemservice.service;

import com.example.problemservice.client.IdentityClient;

import com.example.problemservice.dto.request.LeaderboardUpdateRequest;
import com.example.problemservice.dto.request.notification.NotificationRequest;
import com.example.problemservice.dto.request.profile.SingleProfileInformationRequest;
import com.example.problemservice.dto.response.problemComment.ProblemCommentCreationResponse;
import com.example.problemservice.model.Problem;
import com.example.problemservice.model.ProblemComment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {
    IdentityClient identityClient;

    @Async
    public void upvoteCommentNotification(ProblemComment problemComment, String userUid) {
        try{
            NotificationRequest notificationRequest = new NotificationRequest();
            String userName = identityClient.getSingleProfileInformation(
                            new SingleProfileInformationRequest(userUid))
                    .block().getResult().getDisplayName();
            notificationRequest.setTitle(userName + " has been upvoted your comment");
            notificationRequest.setMessage("");
            notificationRequest.setUserid(problemComment.getUserUuid());
            notificationRequest.setRedirectType("PROBLEM_COMMENT");
            notificationRequest.setRedirectContent("http://localhost:3000/problems/" + problemComment.getProblem().getProblemId());
            identityClient.postNotifications(notificationRequest).block().getResult().getMessage();
        }catch (Exception ignored){}
    }

    @Async
    public void createCommentNotification(ProblemCommentCreationResponse response) {
        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setTitle(response.getUsername() + "has just replied to your comment");
        notificationRequest.setMessage(response.getContent());
        notificationRequest.setUserid(UUID.fromString(response.getUserUuid()));
        notificationRequest.setRedirectType("PROBLEM_COMMENT");
        notificationRequest.setRedirectContent("http://localhost:3000/problems/"+ response.getProblemId());
        try{
            System.out.println("Send problem comment noti");
            identityClient.postNotifications(notificationRequest).block().getResult().getMessage();
        }
        catch (Exception ignore)
        {        }
    }
    @Async
    public void solveProblemNotification(Problem problem, UUID userUid) {
        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setUserid(userUid);
        notificationRequest.setTitle("\uD83C\uDF89 Congratulations on Solving a Problem!");
        notificationRequest.setMessage("Great job! You've successfully solved the problem \"" + problem.getProblemName() + "\". " +
                "This is a fantastic step forward in your coding journey. " +
                "Keep up the momentum and continue challenging yourself with even more problems. " +
                "Remember, every solved challenge brings you closer to mastery. 🔥");
        notificationRequest.setRedirectType("SOLVED_PROBLEM");
        notificationRequest.setRedirectContent("http://localhost:3000/problems/" + problem.getProblemId());
        try{
            identityClient.postNotifications(notificationRequest).block().getResult().getMessage();
        }
        catch (Exception ignore){
        }
    }

    @Async
    public void updateLeaderboard(int score, String level, UUID userId){
        LeaderboardUpdateRequest request = new LeaderboardUpdateRequest();
        request.setType("problem");
        request.setNewScore((long) score);
        request.setUserId(userId.toString());
        switch (level) {
            case "easy" -> request.getProblemStat().setEasy(1);
            case "medium" -> request.getProblemStat().setMedium(1);
            case "hard" -> request.getProblemStat().setHard(1);
        }
        identityClient.updateLeaderboard(request).block();
    }


}
