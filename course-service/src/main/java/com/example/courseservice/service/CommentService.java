package com.example.courseservice.service;


import com.example.courseservice.dto.request.comment.CommentCreationRequest;
import com.example.courseservice.dto.request.comment.CommentModifyRequest;
import com.example.courseservice.dto.response.Comment.CommentResponse;
import com.example.courseservice.exception.AppException;
import com.example.courseservice.exception.ErrorCode;
import com.example.courseservice.mapper.CommentMapper;
import com.example.courseservice.model.Comment;
import com.example.courseservice.model.Course;
import com.example.courseservice.model.Reaction;
import com.example.courseservice.model.compositeKey.ReactionID;
import com.example.courseservice.repository.CommentRepository;
import com.example.courseservice.repository.CourseRepository;
import com.example.courseservice.repository.ReactionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults( level = AccessLevel.PRIVATE, makeFinal = true)
public class CommentService {
    private final CommentRepository commentRepository;
    private final CourseRepository courseRepository;
    private final CommentMapper commentMapper;
    private final ReactionRepository reactionRepository;

    public List<CommentResponse> getComments(UUID courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(()-> new AppException(ErrorCode.COURSE_NOT_EXISTED));
        List<Comment> comments = commentRepository.findByTopicAndParentCommentIsNull(course.getTopic());
        return comments.stream().map(commentMapper::toResponse).collect(Collectors.toList());
    }

    public CommentResponse addComment(UUID courseId, CommentCreationRequest request, UUID userId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(()-> new AppException(ErrorCode.COURSE_NOT_EXISTED));
        Comment comment = new Comment();
        comment.setTopic(course.getTopic());
        comment.setContent(request.getContent());
        comment.setUserId(userId);

        if (request.getRepliedCommentId() != null) {
            Comment repliedComment = commentRepository.findById(request.getRepliedCommentId())
                .orElseThrow(()-> new AppException(ErrorCode.COMMENT_NOT_FOUND));

            comment.setRepliedComment(repliedComment);
        }
        if (request.getParentCommentId() != null) {
            Comment parentComment = commentRepository.findById(request.getParentCommentId())
                    .orElseThrow(()-> new AppException(ErrorCode.COMMENT_NOT_FOUND));

            if (parentComment.getParentComment() != null) {
                throw new AppException(ErrorCode.INVALID_COMMENT);
            }
            comment.setParentComment(parentComment);
        }

        Comment result = commentRepository.save(comment);

        return commentMapper.toResponse(result);
    }
    public CommentResponse ModifyComment(UUID userId, CommentModifyRequest request) {
        Comment comment = commentRepository.findById(request.getCommentId())
                .orElseThrow(()-> new AppException(ErrorCode.COMMENT_NOT_FOUND));
        if (!comment.getUserId().equals(userId)) {
            throw new AppException(ErrorCode.INVALID_COMMENT);
        }
        comment.setContent(request.getContent());
        comment = commentRepository.save(comment);

        return commentMapper.toResponse(comment);
    }

    public CommentResponse upvoteComment(UUID userId, UUID commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(()-> new AppException(ErrorCode.COMMENT_NOT_FOUND));
        if (!comment.getUserId().equals(userId)) {
            throw new AppException(ErrorCode.INVALID_COMMENT);
        }

        ReactionID id = new ReactionID(userId,commentId);
        Reaction reactions = new Reaction(id,comment);

        comment.getReactions().add(reactions);

        comment = commentRepository.save(comment);

        return commentMapper.toResponse(comment);
    }

    public CommentResponse cancelUpvoteComment(UUID userId, UUID commentId) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(()-> new AppException(ErrorCode.COMMENT_NOT_FOUND));

        if (!comment.getUserId().equals(userId)) {
            throw new AppException(ErrorCode.INVALID_COMMENT);
        }

        Reaction reactions = reactionRepository.findByReactionID_UserIdAndComment(userId,comment);

        comment.getReactions().remove(reactions);

        comment = commentRepository.save(comment);

        return commentMapper.toResponse(comment);
    }

    public Boolean removeComment(UUID commentId, UUID userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(()-> new AppException(ErrorCode.COMMENT_NOT_FOUND));
        if (!comment.getUserId().equals(userId)) {
            throw new AppException(ErrorCode.INVALID_COMMENT);
        }

        commentRepository.delete(comment);
        commentRepository.flush();
        return true;
    }

}
