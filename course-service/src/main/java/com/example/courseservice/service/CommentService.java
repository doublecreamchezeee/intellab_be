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
import com.example.courseservice.utils.ParseUUID;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults( level = AccessLevel.PRIVATE, makeFinal = true)
public class CommentService {
    private final CommentRepository commentRepository;
    private final CourseRepository courseRepository;
    private final CommentMapper commentMapper;
    private final ReactionRepository reactionRepository;

    public Page<CommentResponse> getComments(UUID courseId, UUID userId, Pageable pageable, Pageable childrenPageable) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(()-> new AppException(ErrorCode.COURSE_NOT_EXISTED));
        Page<Comment> comments = commentRepository.findByTopicAndParentCommentIsNull(course.getTopic(), pageable);


        System.out.println(userId);

        return comments.map( comment -> {
            CommentResponse commentResponse = commentMapper.toResponse(comment);

            Page<Comment> childrenComments = commentRepository.findByParentComment(comment, childrenPageable);

            commentResponse.setComments(childrenComments.map(childrenComment-> {

                CommentResponse childrenCommentResponse = commentMapper.toResponse(childrenComment);

                childrenCommentResponse.setIsOwner(userId != null ? childrenComment.getUserId().equals(userId) : false);

                childrenCommentResponse.setIsUpvoted(childrenComment.getReactions()
                        .stream().anyMatch(reaction -> reaction.getReactionID().getUserId().equals(userId)));

                return childrenCommentResponse;
            }));

            commentResponse.setIsOwner(userId != null ? comment.getUserId().equals(userId) : false);

            commentResponse.setIsUpvoted(comment.getReactions()
                    .stream().anyMatch(reaction -> reaction.getReactionID().getUserId().equals(userId)));

            return commentResponse;
                });
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
        else
        {
            if (request.getRepliedCommentId() != null) {
                Comment parentComment = commentRepository.findById(request.getRepliedCommentId())
                        .orElseThrow(()-> new AppException(ErrorCode.COMMENT_NOT_FOUND));
                while (parentComment.getParentComment() != null) {
                    parentComment = parentComment.getParentComment();
                }
                comment.setParentComment(parentComment);
            }
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
        comment.setLastModified(Instant.now());

        comment = commentRepository.save(comment);

        return commentMapper.toResponse(comment);
    }

    public CommentResponse upvoteComment(UUID userId, UUID commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(()-> new AppException(ErrorCode.COMMENT_NOT_FOUND));


        ReactionID id = new ReactionID(userId,commentId);
        Reaction reactions = new Reaction(id,comment);

        comment.getReactions().add(reactions);

        comment = commentRepository.save(comment);

        comment.setNumberOfLikes((long) comment.getReactions().size());

        comment = commentRepository.save(comment);

        return commentMapper.toResponse(comment);
    }

    public CommentResponse cancelUpvoteComment(UUID userId, UUID commentId) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(()-> new AppException(ErrorCode.COMMENT_NOT_FOUND));

        if (comment.getReactions().stream().noneMatch(reaction -> reaction.getReactionID().getUserId().equals(userId))) {
            throw new AppException(ErrorCode.INVALID_USER);
        }

        Reaction reactions = reactionRepository.findByReactionID_UserIdAndComment(userId,comment);

        comment.getReactions().remove(reactions);

        comment = commentRepository.save(comment);

        comment.setNumberOfLikes((long) comment.getReactions().size());

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

    public Page<CommentResponse> getChildrenComments(UUID commentId, UUID userId, Pageable pageable) {
        Comment parent = commentRepository.findById(commentId)
                .orElseThrow(()-> new AppException(ErrorCode.COMMENT_NOT_FOUND));

        Page<Comment> children = commentRepository.findByParentComment(parent, pageable);

        return children.map(childrenComment-> {

            CommentResponse childrenCommentResponse = commentMapper.toResponse(childrenComment);

            childrenCommentResponse.setIsOwner(userId != null ? childrenComment.getUserId().equals(userId) : false);

            childrenCommentResponse.setIsUpvoted(childrenComment.getReactions()
                    .stream().anyMatch(reaction -> reaction.getReactionID().getUserId().equals(userId)));

            return childrenCommentResponse;
        });
    }

    public CommentResponse getComment(UUID commentId, UUID userId, Pageable pageable) {
        Comment parent = commentRepository.findById(commentId)
                .orElseThrow(()-> new AppException(ErrorCode.COMMENT_NOT_FOUND));

        Page<Comment> children = commentRepository.findByParentComment(parent, pageable);

        CommentResponse result = commentMapper.toResponse(parent);

        result.setComments(children.map(childrenComment-> {

            CommentResponse childrenCommentResponse = commentMapper.toResponse(childrenComment);

            childrenCommentResponse.setIsOwner(userId != null ? childrenComment.getUserId().equals(userId) : false);

            childrenCommentResponse.setIsUpvoted(childrenComment.getReactions()
                    .stream().anyMatch(reaction -> reaction.getReactionID().getUserId().equals(userId)));

            return childrenCommentResponse;
        }));

        return result;
    }

}
