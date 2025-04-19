package com.example.courseservice.service;


import com.example.courseservice.client.IdentityClient;
import com.example.courseservice.dto.request.LeaderboardUpdateRequest;
import com.example.courseservice.dto.request.notification.NotificationRequest;
import com.example.courseservice.dto.response.Comment.CommentResponse;
import com.example.courseservice.model.Comment;
import com.example.courseservice.model.Course;
import com.example.courseservice.model.Review;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class NotificationService {
    FirestoreService firestoreService;
    IdentityClient identityClient;


    @Async
    void upvoteCommentNotification(Comment comment, UUID sessionUserId) {
        NotificationRequest request = new NotificationRequest();
        request.setUserid(comment.getUserId());
        request.setTitle("Your comment has just been upvote");
        String userName = firestoreService.getUsername(sessionUserId);
        request.setMessage("Your comment has just been upvote by [@" + userName + "]");
        request.setRedirectType("COURSE_COMMENT");
        request.setRedirectContent("/course/" + comment.getTopic().getCourse().getCourseId()
        + "?commentId=" + comment.getCommentId());
        try
        {
            identityClient.postNotifications(request).block().getResult().getMessage();
        }
        catch (Exception ignore)
        {
        }
    }
    @Async
    void commentNotification(CommentResponse response, Comment repliedComment, UUID courseId) {
        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setTitle("[@" + response.getUserName() + "] replied to your comment");
        notificationRequest.setMessage(response.getContent());
        notificationRequest.setUserid(repliedComment.getUserId());
        notificationRequest.setRedirectType("COURSE_COMMENT");
        notificationRequest.setRedirectContent("/course/" + courseId
        +  "?commentId=" + response.getCommentId());
        System.out.println(notificationRequest.getTitle());
        try{
            identityClient.postNotifications(notificationRequest).block().getResult().getMessage();
        } catch (Exception e) {
            System.err.println("Error while created comment notification: " + e.getMessage());
        }
    }

    @Async
    void reviewNotification(Review review, UUID sessionUserId, UUID courseId) {
        try {
            String userName = firestoreService.getUsername(sessionUserId);
            NotificationRequest notificationRequest = new NotificationRequest();
            notificationRequest.setTitle("[@" + userName + "] has just review your course");
            notificationRequest.setMessage(review.getComment());
            notificationRequest.setUserid(review.getCourse().getUserId());
            notificationRequest.setRedirectType("COURSE_REVIEW");
            notificationRequest.setRedirectContent("/course/"+ courseId + "?reviewId=" + review.getReviewId());
            identityClient.postNotifications(notificationRequest).block().getResult().getMessage();
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    @Async
    void uploadLeaderBoard(UUID userId, Course course)
    {
        LeaderboardUpdateRequest leaderboardUpdateRequest = new LeaderboardUpdateRequest();
        leaderboardUpdateRequest.setUserId(userId.toString());
        leaderboardUpdateRequest.setType("course");
        leaderboardUpdateRequest.setAdditionalScore((long)course.getScore());

        switch (course.getLevel()) {
            case "Beginner" -> leaderboardUpdateRequest.getCourseStat().setBeginner(1);
            case "Intermediate" -> leaderboardUpdateRequest.getCourseStat().setIntermediate(1);
            case "Advanced" -> leaderboardUpdateRequest.getCourseStat().setAdvanced(1);
        }
        try{
            identityClient.updateLeaderboard(leaderboardUpdateRequest).block();
        }
        catch(Exception e){
            System.out.println("Error in updating leaderboard: " + e);
        }
    }
}
