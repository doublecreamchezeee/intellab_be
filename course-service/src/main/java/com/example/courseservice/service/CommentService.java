package com.example.courseservice.service;


import com.example.courseservice.client.IdentityClient;
import com.example.courseservice.dto.request.comment.CommentCreationRequest;
import com.example.courseservice.dto.request.comment.CommentModifyRequest;
import com.example.courseservice.dto.request.profile.MultipleProfileInformationRequest;
import com.example.courseservice.dto.request.profile.SingleProfileInformationRequest;
import com.example.courseservice.dto.response.Comment.CommentResponse;
import com.example.courseservice.dto.response.profile.MultipleProfileInformationResponse;
import com.example.courseservice.dto.response.profile.SingleProfileInformationResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
@FieldDefaults( level = AccessLevel.PRIVATE, makeFinal = true)
public class CommentService {
    private final CommentRepository commentRepository;
    private final CourseRepository courseRepository;
    private final CommentMapper commentMapper;
    private final ReactionRepository reactionRepository;
    private final IdentityClient identityClient;
    private final FirestoreService firestoreService;
    private final NotificationService notificationService;

    public Page<CommentResponse> getComments(UUID courseId, UUID userId, Pageable pageable, Pageable childrenPageable) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(()-> new AppException(ErrorCode.COURSE_NOT_EXISTED));
        Page<Comment> comments = commentRepository.findByTopicAndParentCommentIsNull(course.getTopic(), pageable);

        List<UUID> uuids = extractUuidFromComments(comments.getContent());

        List<String> uids = uuids.stream().map(uuid -> {
            try {
                return firestoreService.getUserById(uuid.toString()).getUid();
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).toList();

        MultipleProfileInformationResponse profileInformation = Objects.requireNonNull(identityClient.getMultipleProfileInformation(
                new MultipleProfileInformationRequest(
                        uids
                )
        ).block()).getResult();

        Page<CommentResponse> response = comments.map( comment -> {
            CommentResponse commentResponse = commentMapper.toResponse(comment);

            Page<Comment> childrenComments = commentRepository.findByParentComment(comment, childrenPageable);

            commentResponse.setComments(childrenComments.map(childrenComment-> {

                CommentResponse childrenCommentResponse = commentMapper.toResponse(childrenComment);

                childrenCommentResponse.setIsOwner(userId != null ? childrenComment.getUserId().equals(userId) : false);

                childrenCommentResponse.setIsUpvoted(childrenComment.getReactions()
                        .stream().anyMatch(reaction -> reaction.getReactionID().getUserId().equals(userId) && reaction.getActive()));

                return childrenCommentResponse;
            }));

            commentResponse.setIsOwner(userId != null ? comment.getUserId().equals(userId) : false);

            commentResponse.setIsUpvoted(comment.getReactions()
                    .stream().anyMatch(reaction -> reaction.getReactionID().getUserId().equals(userId) && reaction.getActive()));

            return commentResponse;
        });

        if(profileInformation != null && !profileInformation.getProfiles().isEmpty())
        {
            response = commentMapper.mapProfileToCommentResponsePage(response, profileInformation);
        }

        return response;
    }

    @Transactional
    public CommentResponse addComment(UUID courseId, CommentCreationRequest request, UUID userId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(()-> new AppException(ErrorCode.COURSE_NOT_EXISTED));
        Comment comment = new Comment();
        comment.setTopic(course.getTopic());
        comment.setContent(request.getContent());
        comment.setUserId(userId);

        Comment repliedComment = null;
        if (request.getRepliedCommentId() != null) {
            repliedComment = commentRepository.findById(request.getRepliedCommentId())
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

        CommentResponse response =  commentMapper.toResponse(result);
        response.setIsOwner(true);
        response.setIsUpvoted(false);

        SingleProfileInformationResponse profile = null;
        try
        {
            profile = identityClient.getSingleProfileInformation(
                    new SingleProfileInformationRequest(
                            firestoreService.getUserById(response.getUserId().toString()).getUid()
                    ))
                    .block()
                    .getResult();
        }
        catch (Exception e)
        {
            System.err.println("Error while get SingleProfile: " + e);
        }

        if (profile != null)
        {
            response.setUserUid(profile.getUserId());
            response.setUserName(profile.getDisplayName());
            response.setAvatarUrl(profile.getPhotoUrl());
        }

        if (repliedComment != null && !result.getUserId().equals(repliedComment.getUserId()))
        {
            notificationService.commentNotification(response,repliedComment);
        }

        return response;
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

        CommentResponse response = commentMapper.toResponse(comment);

        response.setIsOwner(true);
        response.setIsUpvoted(false);

        SingleProfileInformationResponse profile = null;
        try
        {
            profile = identityClient.getSingleProfileInformation(
                            new SingleProfileInformationRequest(
                                    firestoreService.getUserById(response.getUserId().toString()).getUid()
                            ))
                    .block()
                    .getResult();
        }
        catch (Exception e)
        {
            System.err.println("Error while get SingleProfile: " + e);
        }

        if (profile != null)
        {
            response.setUserUid(profile.getUserId());
            response.setUserName(profile.getDisplayName());
            response.setAvatarUrl(profile.getPhotoUrl());
        }
        return response;
    }

    @Transactional
    public Long upvoteComment(UUID userId, UUID commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(()-> new AppException(ErrorCode.COMMENT_NOT_FOUND));


        ReactionID id = new ReactionID(userId,commentId);
        Boolean isExisted = reactionRepository.existsById(id);

        Reaction reactions = new Reaction(id, comment, true);
        comment.getReactions().add(reactions);
        comment.setNumberOfLikes(comment.getReactions()
                .stream().filter(Reaction::getActive)
                .count());
        comment = commentRepository.save(comment);

//        CommentResponse response = commentMapper.toResponse(comment);
//        response.setIsOwner(userId != null ? comment.getUserId().equals(userId) : false);
//
//        response.setIsUpvoted(comment.getReactions()
//                .stream().anyMatch(
//                        reaction -> reaction.getReactionID().getUserId().equals(userId) && reaction.getActive()
//                        ));

        if (!userId.equals(comment.getUserId()) && !isExisted) {
            notificationService.upvoteCommentNotification(comment,userId);
        }
        return comment.getNumberOfLikes();
    }



    public Long cancelUpvoteComment(UUID userId, UUID commentId) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(()-> new AppException(ErrorCode.COMMENT_NOT_FOUND));

        if (comment.getReactions().stream().noneMatch(reaction -> reaction.getReactionID().getUserId().equals(userId))) {
            throw new AppException(ErrorCode.INVALID_USER);
        }

        Reaction reaction = reactionRepository.findByReactionID_UserIdAndComment(userId,comment);

        reaction.setActive(false);
        reactionRepository.save(reaction);

        comment.setNumberOfLikes(comment.getReactions()
                .stream()
                .filter(Reaction::getActive)
                .count());

        comment = commentRepository.save(comment);
        return comment.getNumberOfLikes();
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

        List<UUID> uuids = extractUuidFromComments(children.getContent());

        List<String> uids = uuids.stream().map(uuid -> {
            try {
                return firestoreService.getUserById(uuid.toString()).getUid();
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).toList();

        Page<CommentResponse> response = children.map(childrenComment-> {

            CommentResponse childrenCommentResponse = commentMapper.toResponse(childrenComment);

            childrenCommentResponse.setIsOwner(userId != null ? childrenComment.getUserId().equals(userId) : false);

            childrenCommentResponse.setIsUpvoted(childrenComment.getReactions()
                    .stream().anyMatch(reaction -> reaction.getReactionID().getUserId().equals(userId) && reaction.getActive()));

            return childrenCommentResponse;
        });

        MultipleProfileInformationResponse profiles = null;
        try {
            profiles = identityClient.getMultipleProfileInformation(
                    new MultipleProfileInformationRequest(uids)
            ).block().getResult();
        }
        catch (Exception e)
        {
            System.err.println("Error while get MultipleProfile: " + e);
        }
        if (profiles != null) {
            response = commentMapper.mapProfileToCommentResponsePage(response, profiles);
        }

        return response;
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
                    .stream().anyMatch(reaction -> reaction.getReactionID().getUserId().equals(userId) && reaction.getActive()));

            return childrenCommentResponse;
        }));

        List<UUID> uuids = new ArrayList<>();
        uuids.add(parent.getUserId());
        if (!children.isEmpty()){
            uuids.addAll(extractUuidFromComments(children.getContent()));
        }

        List<String> uids = uuids.stream().map(uuid -> {
            try {
                return firestoreService.getUserById(uuid.toString()).getUid();
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).toList();

        MultipleProfileInformationResponse profiles = null;
        try {
            profiles = identityClient.getMultipleProfileInformation(
                    new MultipleProfileInformationRequest(uids)
            ).block().getResult();
        }
        catch (Exception e)
        {
            System.err.println("Error while get MultipleProfile: " + e);
        }
        if (profiles != null) {
            result = commentMapper.mapProfileToCommentResponse(result, profiles);
        }

        return result;
    }

    private List<UUID> extractUuidFromComments(List<Comment> comments) {
        Set<UUID> userUuids = new HashSet<>();

        for(Comment comment : comments) {
            userUuids.add(comment.getUserId());
            if (comment.getComments() != null) {
                for (Comment childComment : comment.getComments()) {
                    userUuids.add(childComment.getUserId());
                }
            }
        }

        return List.copyOf(userUuids);
    }

}
