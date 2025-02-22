package com.example.courseservice.mapper;

import com.example.courseservice.dto.response.Comment.CommentResponse;
import com.example.courseservice.model.Comment;
import com.example.courseservice.model.Firestore.User;
import com.example.courseservice.service.FirestoreService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CommentMapper {
    @Autowired
    private final FirestoreService firestoreService;

    @SneakyThrows
    public CommentResponse toResponse(Comment comment) {
        if (comment == null) {
            return null;
        }

        Boolean isUpVoted = false;
        Boolean isModified = !comment.getCreated().equals(comment.getLastModified());
        Long upvote = (long) comment.getReactions().size();

        return CommentResponse.builder()
                .commentId(comment.getCommentId())
                .content(comment.getContent())
                .numberOfLikes(upvote)
                .created(comment.getCreated())
                .lastModified(comment.getLastModified())
                .userId(comment.getUserId())
                .repliedCommentId(comment.getRepliedComment() == null ? null : comment.getRepliedComment().getCommentId())
                .parentCommentId(comment.getParentComment() == null ? null : comment.getParentComment().getCommentId())
                .isUpVoted(isUpVoted)
                .isModified(isModified)
                .userName(getUserName(comment.getUserId()))
                // Ánh xạ đệ quy cho danh sách comments con
                .comments(comment.getComments() != null
                        ? comment.getComments().stream()
                        .map(this::toResponse)
                        .collect(Collectors.toList())
                        : null)
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

}