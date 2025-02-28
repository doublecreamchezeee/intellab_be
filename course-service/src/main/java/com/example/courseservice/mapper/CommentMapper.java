package com.example.courseservice.mapper;

import com.example.courseservice.client.IdentityClient;
import com.example.courseservice.dto.ApiResponse;
import com.example.courseservice.dto.request.profile.SingleProfileInformationRequest;
import com.example.courseservice.dto.response.Comment.CommentResponse;
import com.example.courseservice.dto.response.profile.SingleProfileInformationResponse;
import com.example.courseservice.model.Comment;
import com.example.courseservice.model.Firestore.User;
import com.example.courseservice.service.FirestoreService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommentMapper {
    @Autowired
    private final FirestoreService firestoreService;
    private final IdentityClient identityClient;

    @SneakyThrows
    public CommentResponse toResponse(Comment comment) {
        Boolean isModified = !comment.getCreated().equals(comment.getLastModified());
        String uid = getUserUid(comment.getUserId());

        String url = null;
        String username = null;

        ApiResponse<SingleProfileInformationResponse> userResponse = null;

        if (uid != null) {
            try {
                userResponse = identityClient
                        .getSingleProfileInformation(
                                new SingleProfileInformationRequest(
                                        uid
                                )
                        ).block();

            } catch (Exception e) {
                log.error("Error when getting user info {}", uid, e);
            }

            if (userResponse != null) {
                SingleProfileInformationResponse user = userResponse.getResult();
                try
                {
                    username = user.getDisplayName();
                }
                catch (Exception ignored) {

                }
                try {
                    url = user.getPhotoUrl();
                }
                catch (Exception ignored) {
                }
            }

        }



        return CommentResponse.builder()
                .commentId(comment.getCommentId())
                .content(comment.getContent())
                .numberOfLikes(comment.getNumberOfLikes())
                .created(comment.getCreated())
                .lastModified(comment.getLastModified())
                .userId(comment.getUserId())
                .repliedCommentId(comment.getRepliedComment() == null ? null : comment.getRepliedComment().getCommentId())
                .parentCommentId(comment.getParentComment() == null ? null : comment.getParentComment().getCommentId())
                .isModified(isModified)
                .userName(username != null ? username : getUserName(comment.getUserId()))
                .userUid(uid)
                .avatarUrl(url)
                .build();
    }

    private String getUserName(UUID userId) {
        try {
            User user = firestoreService.getUserById(userId.toString());
            return user.getLastName() + " " + user.getFirstName();
        } catch (Exception e) {
            return "Unknown User";
        }
    }

    private String getUserUid(UUID userId) {
        try {
            User user = firestoreService.getUserById(userId.toString());
            return user.getUid();
        } catch (Exception e) {
            return null;
        }
    }

}