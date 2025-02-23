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
        Boolean isModified = !comment.getCreated().equals(comment.getLastModified());


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
                .userName(getUserName(comment.getUserId()))
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