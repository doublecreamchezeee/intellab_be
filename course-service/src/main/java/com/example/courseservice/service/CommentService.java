package com.example.courseservice.service;


import com.example.courseservice.dto.request.comment.CommentCreationRequest;
import com.example.courseservice.dto.response.Comment.CommentResponse;
import com.example.courseservice.exception.AppException;
import com.example.courseservice.exception.ErrorCode;
import com.example.courseservice.mapper.CommentMapper;
import com.example.courseservice.mapper.CourseMapper;
import com.example.courseservice.model.Comment;
import com.example.courseservice.model.Course;
import com.example.courseservice.repository.CommentRepository;
import com.example.courseservice.repository.CourseRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults( level = AccessLevel.PRIVATE, makeFinal = true)
public class CommentService {
    private final CommentRepository commentRepository;
    private final CourseRepository courseRepository;
    private final CommentMapper commentMapper;
    private final FirestoreService firestoreService;

    public List<CommentResponse> getComments(UUID courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(()-> new AppException(ErrorCode.COURSE_NOT_EXISTED));
        List<Comment> comments = commentRepository.findByTopicAndParentCommentIsNull(course.getTopic());
        return comments.stream().map(commentMapper::toResponse).collect(Collectors.toList());
    }

    public CommentResponse addComment(UUID courseId, CommentCreationRequest request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(()-> new AppException(ErrorCode.COURSE_NOT_EXISTED));
        Comment comment = new Comment();
        comment.setTopic(course.getTopic());
        comment.setContent(request.getContent());
        if (request.getReplyTo() != null) {
        Comment parentComment = commentRepository.findById(request.getReplyTo())
                .orElseThrow(()-> new AppException(ErrorCode.COMMENT_NOT_FOUND));
        comment.setParentComment(parentComment);
        }
        Comment result = commentRepository.save(comment);

        return commentMapper.toResponse(result);
    }
}
