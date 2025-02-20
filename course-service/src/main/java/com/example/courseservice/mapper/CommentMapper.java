package com.example.courseservice.mapper;

import com.example.courseservice.dto.response.Comment.CommentResponse;
import com.example.courseservice.model.Comment;
import com.example.courseservice.model.Firestore.User;
import com.example.courseservice.service.CommentService;
import com.example.courseservice.service.FirestoreService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
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

        return CommentResponse.builder()
                .commentId(comment.getCommentId())
                .content(comment.getContent())
                .numberOfLikes(comment.getNumberOfLikes())
                .created(comment.getCreated())
                .userId(comment.getUserId())
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