package com.example.problemservice.service;

import com.example.problemservice.client.IdentityClient;
import com.example.problemservice.dto.request.notification.NotificationRequest;
import com.example.problemservice.dto.request.problemComment.ProblemCommentCreationRequest;
import com.example.problemservice.dto.request.problemComment.ProblemCommentUpdateRequest;
import com.example.problemservice.dto.request.profile.MultipleProfileInformationRequest;
import com.example.problemservice.dto.request.profile.SingleProfileInformationRequest;
import com.example.problemservice.dto.response.problemComment.DetailsProblemCommentResponse;
import com.example.problemservice.dto.response.problemComment.ProblemCommentCreationResponse;
import com.example.problemservice.dto.response.problemComment.ProblemCommentUpdateResponse;
import com.example.problemservice.dto.response.problemComment.SingleProblemCommentResponse;
import com.example.problemservice.dto.response.profile.MultipleProfileInformationResponse;
import com.example.problemservice.dto.response.profile.SingleProfileInformationResponse;
import com.example.problemservice.exception.AppException;
import com.example.problemservice.exception.ErrorCode;
import com.example.problemservice.mapper.ProblemCommentMapper;
import com.example.problemservice.mapper.custom.CustomProblemCommentMapper;
import com.example.problemservice.model.Problem;
import com.example.problemservice.model.ProblemComment;
import com.example.problemservice.model.ProblemCommentReaction;
import com.example.problemservice.model.composite.ProblemCommentReactionId;
import com.example.problemservice.repository.ProblemCommentReactionRepository;
import com.example.problemservice.repository.ProblemCommentRepository;
import com.example.problemservice.repository.ProblemRepository;
import com.example.problemservice.utils.ParseUUID;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProblemCommentService {
    private final ProblemCommentRepository problemCommentRepository;
    private final ProblemCommentMapper problemCommentMapper;
    private final ProblemRepository problemRepository;
    private final CustomProblemCommentMapper customProblemCommentMapper;
    private final ProfileService profileService;
    private final ProblemCommentReactionRepository problemCommentReactionRepository;
    private final IdentityClient identityClient;

    // create a new comment
    public ProblemCommentCreationResponse createProblemComment(String userUid, ProblemCommentCreationRequest request) {
        Problem problem = problemRepository.findById(
                UUID.fromString(
                        request.getProblemId()
                )
        ).orElseThrow(
                () -> new AppException(ErrorCode.PROBLEM_NOT_EXIST)
        );

        ProblemComment problemComment = problemCommentMapper.toProblemComment(request);

        problemComment.setProblem(problem);

        problemComment.setUserUid(userUid);

        problemComment.setUserUuid(
                ParseUUID.normalizeUID(
                        userUid
                )
        );

        problemComment.setNumberOfLikes(0);
        problemComment.setChildrenComments(null);
        problemComment.setReplyingComments(null);
        problemComment.setIsModified(false);

        // Set parent comment
        if (request.getParentCommentId() != null) {
            UUID parentCommentId = null;

            try {
                parentCommentId = UUID.fromString(
                        request.getParentCommentId()
                );
            } catch (Exception e) {
                throw new AppException(ErrorCode.INVALID_PARENT_COMMENT_ID);
            }

            ProblemComment parentComment = problemCommentRepository.findById(
                    parentCommentId
            ).orElseThrow(
                    () -> new AppException(ErrorCode.PARENT_COMMENT_NOT_EXIST)
            );

            if (parentComment.getProblem().getProblemId() != problem.getProblemId()) {
                throw new AppException(ErrorCode.PROBLEM_ID_NOT_SAME_AS_PROBLEM_ID_OF_PARENT_COMMENT);
            }

            problemComment.setParentComment(parentComment);


        } else {
            problemComment.setParentComment(null);
        }

        if (request.getParentCommentId() != null && request.getReplyToCommentId() == null) {
            request.setReplyToCommentId(request.getParentCommentId());
        }

        // Set replied comment
        if (request.getReplyToCommentId() != null)  {
            UUID repliedCommentId = null;
            try {
                repliedCommentId = UUID.fromString(
                        request.getReplyToCommentId()
                );
            } catch (Exception e) {
                throw new AppException(ErrorCode.INVALID_REPLIED_COMMENT_ID);
            }

            ProblemComment repliedComment = problemCommentRepository.findById(
                    repliedCommentId
            ).orElseThrow(
                    () -> new AppException(ErrorCode.REPLIED_COMMENT_NOT_EXIST)
            );

            if (repliedComment.getProblem().getProblemId() != problem.getProblemId()) {
                throw new AppException(ErrorCode.PROBLEM_ID_NOT_SAME_AS_PROBLEM_ID_OF_REPLIED_COMMENT);
            }

            problemComment.setRepliedComment(repliedComment);



            //reset parent comment if exist replied comment
            problemComment.setParentComment(
                    repliedComment.getParentComment() != null
                            ? repliedComment.getParentComment() // case replied comment has parent comment
                            : repliedComment // case replied comment is the parent comment
            );
        } else {
            problemComment.setRepliedComment(null);
        }

        problemComment = problemCommentRepository.save(problemComment);

        ProblemCommentCreationResponse response = problemCommentMapper.toProblemCommentCreationResponse(problemComment);

        SingleProfileInformationResponse profileInformation = profileService.getSingleProfileInformation(
                problemComment.getUserUid()
        );

        if (profileInformation != null) {
            response.setUsername(profileInformation.getDisplayName());
            response.setUserEmail(profileInformation.getEmail());
            response.setUserAvatar(profileInformation.getPhotoUrl());
        } else {
            response.setUsername(null);
            response.setUserEmail(null);
            response.setUserAvatar(null);
        }

        if (response.getReplyToCommentId() != null) {
            NotificationRequest notificationRequest = new NotificationRequest();
            notificationRequest.setUserid(response.getReplyToCommentId());
            notificationRequest.setTitle(response.getUsername() + "has just replied to your comment.");
            notificationRequest.setMessage(response.getContent());
            try{
                identityClient.postNotifications(notificationRequest);
            }
            catch (Exception ignore)
            {
            }
        }

        response.setIsUpVoted(false);

        return response;
    }

    public ProblemCommentUpdateResponse updateProblemComment(String userUid, UUID problemCommentId,ProblemCommentUpdateRequest request) {
        ProblemComment problemComment = problemCommentRepository.findById(
                problemCommentId
        ).orElseThrow(
                () -> new AppException(ErrorCode.COMMENT_NOT_EXIST)
        );

        if (!problemComment.getUserUid().equals(userUid)) {
            throw new AppException(ErrorCode.USER_DONT_HAVE_PERMISSION_TO_UPDATE_COMMENT);
        }

        problemCommentMapper.updateProblemCommentFromRequest(request, problemComment);

        problemComment.setIsModified(true);

        problemComment = problemCommentRepository.save(problemComment);

        ProblemCommentUpdateResponse response =  problemCommentMapper.toProblemCommentUpdateResponse(problemComment);

        SingleProfileInformationResponse profileInformation = profileService.getSingleProfileInformation(
                problemComment.getUserUid()
        );

        if (profileInformation != null) {
            response.setUsername(profileInformation.getDisplayName());
            response.setUserEmail(profileInformation.getEmail());
            response.setUserAvatar(profileInformation.getPhotoUrl());
        } else {
            response.setUsername(null);
            response.setUserEmail(null);
            response.setUserAvatar(null);
        }

        // check if user upvoted his own comment
        if (userUid != null) {
            Boolean isReactionExisted = problemCommentReactionRepository.existsByProblemComment_CommentIdAndReactionId_UserUuid(
                    problemComment.getCommentId(),
                    ParseUUID.normalizeUID(userUid)
            );

            response.setIsUpVoted(isReactionExisted);
        }

        return response;

    }

    @Transactional
    public void deleteProblemComment(String userUid, UUID commentId) {
        ProblemComment problemComment = problemCommentRepository.findById(
                commentId
        ).orElseThrow(
                () -> new AppException(ErrorCode.COMMENT_NOT_EXIST)
        );

        if (!problemComment.getUserUid().equals(userUid)) {
            throw new AppException(ErrorCode.USER_DONT_HAVE_PERMISSION_TO_DELETE_COMMENT);
        }

        // delete all children comments before delete parent comment
        problemCommentRepository.deleteAllByParentComment_CommentId(commentId);

        problemCommentRepository.delete(problemComment);
    }

    public void softDeleteProblemComment(String userUid, UUID commentId) {
        ProblemComment problemComment = problemCommentRepository.findById(
                commentId
        ).orElseThrow(
                () -> new AppException(ErrorCode.COMMENT_NOT_EXIST)
        );

        if (!problemComment.getUserUid().equals(userUid)) {
            throw new AppException(ErrorCode.USER_DONT_HAVE_PERMISSION_TO_DELETE_COMMENT);
        }

        problemComment.setContent("[This comment has been deleted by the user]");

        problemCommentRepository.save(problemComment);
    }

    public SingleProblemCommentResponse getOneProblemCommentById(String commentId, UUID requestUserUuid) {
        ProblemComment problemComment = problemCommentRepository.findById(
                UUID.fromString(
                        commentId
                )
        ).orElseThrow(
                () -> new AppException(ErrorCode.COMMENT_NOT_EXIST)
        );

        SingleProblemCommentResponse response = problemCommentMapper.toSingleProblemCommentResponse(problemComment);

        SingleProfileInformationResponse profileInformation = profileService.getSingleProfileInformation(
                problemComment.getUserUid()
        );

        if (profileInformation != null) {
            response.setUsername(profileInformation.getDisplayName());
            response.setUserEmail(profileInformation.getEmail());
            response.setUserAvatar(profileInformation.getPhotoUrl());
        } else {
            response.setUsername(null);
            response.setUserEmail(null);
            response.setUserAvatar(null);
        }

        if (requestUserUuid != null) {
            Boolean isReactionExisted = problemCommentReactionRepository.existsByProblemComment_CommentIdAndReactionId_UserUuid(
                    problemComment.getCommentId(),
                    requestUserUuid
            );

            response.setIsUpVoted(isReactionExisted);
        }

        return response;
    }

    public Page<DetailsProblemCommentResponse> getAllProblemCommentByProblemId(
            UUID problemId,
            UUID requestUserUuid,
            Pageable pageable,
            Pageable childrenPageable
    ) {
        Problem problem = problemRepository.findById(problemId)
            .orElseThrow(
                () -> new AppException(ErrorCode.PROBLEM_NOT_EXIST)
        );
        //log.info("pageable: {}", pageable);

        Page<ProblemComment> problemComments = problemCommentRepository.findAllByProblem_ProblemIdAndParentCommentIsNull(problemId, pageable);
        //findAllByProblem_ProblemIdAndParentCommentIsNull(problemId, pageable);

        //log.info("problemComments: {}", problemComments.stream().map(ProblemComment::getNumberOfLikes).toList());

        List<DetailsProblemCommentResponse> responseList = problemComments
                .getContent()
                .stream()
                .map(
                        problemComment -> customProblemCommentMapper
                                .toDetailsProblemCommentResponse(
                                        problemComment,
                                        childrenPageable
                                )
                )
                .toList();

        //log.info("responseList: {}", responseList.stream().map(DetailsProblemCommentResponse::getNumberOfLikes).toList());

        Page<DetailsProblemCommentResponse> responses = new PageImpl<>(responseList, pageable, problemComments.getTotalElements());

        // extract user uid to get profile information from identity service
        List<String> userUids = extractUserUidFrom2DArrayProblemComments(responses.getContent());

        // call identity service API to get profile information
        MultipleProfileInformationResponse profileInformation = profileService.getMultipleProfileInformation(
                new MultipleProfileInformationRequest(
                        userUids
                )
        );

        if (profileInformation == null) {
            return responses;
        }

        // mapping profile information to response
        responses = customProblemCommentMapper.
                mappingProfileInformationToPageProblemComments(
                        responses,
                        profileInformation
                );

        if (requestUserUuid == null) {
            return responses;
        }

        // get all comment ids to get reaction information
        List<UUID> commentIds = extractProblemCommentIdFrom2DArrayProblemComments(
                responses.getContent()
        );

        // get all reaction information
        List<ProblemCommentReaction> reactions = problemCommentReactionRepository.findAllByCommentIdsAndUserUuid(
                commentIds,
                requestUserUuid
        );


        return mappingReactionToPageProblemComments(
                responses,
                reactions
        );

    }

    public DetailsProblemCommentResponse getOneProblemCommentAndItsChildrenById(String commentId, UUID requestUserUuid, Pageable pageable) {
        ProblemComment problemComment = problemCommentRepository.findById(
                UUID.fromString(
                        commentId
                )
        ).orElseThrow(
                () -> new AppException(ErrorCode.COMMENT_NOT_EXIST)
        );
        DetailsProblemCommentResponse response = customProblemCommentMapper.toDetailsProblemCommentResponse(
                problemComment,
                pageable
        );

        List<String> userUids = extractUserUidFrom2DArrayProblemComments(
                List.of(
                       response
                )
        );

        MultipleProfileInformationResponse profileInformation = profileService.getMultipleProfileInformation(
                new MultipleProfileInformationRequest(
                        userUids
                )
        );

        if (profileInformation == null) {
            return response;
        }

        response = customProblemCommentMapper
                .mappingProfileInformationToOneProblemComments(
                        response,
                        profileInformation
                );

        if (requestUserUuid == null) {
            return response;
        }

        List<UUID> commentIds = extractProblemCommentIdFrom2DArrayProblemComments(
                List.of(
                        response
                )
        );

        List<ProblemCommentReaction> reactions = problemCommentReactionRepository.findAllByCommentIdsAndUserUuid(
                commentIds,
                requestUserUuid
        );

        return mappingReactionToPageProblemComments(
                new PageImpl<>(List.of(response), pageable, 1),
                reactions
        ).getContent().get(0);
    }

    public Page<DetailsProblemCommentResponse> getChildrenCommentOfProblemCommentById(UUID commentId, UUID requestUserUuid, Pageable pageable) {
        ProblemComment problemComment = problemCommentRepository.findById(
                commentId
        ).orElseThrow(
                () -> new AppException(ErrorCode.COMMENT_NOT_EXIST)
        );

        Page<DetailsProblemCommentResponse> responses = problemCommentRepository
                .findAllByParentComment_CommentId(
                        commentId,
                        pageable
                )
                .map(
                        problemCommentChildren ->
                                customProblemCommentMapper.toDetailsProblemCommentResponse(
                                        problemCommentChildren,
                                        pageable
                                )
                );

        // extract user uid to get profile information from identity service
        List<String> userUids = extractUserUidFrom1DArrayProblemComments(responses.getContent());

        // call identity service API to get profile information
        MultipleProfileInformationResponse profileInformation = profileService.getMultipleProfileInformation(
                new MultipleProfileInformationRequest(
                        userUids
                )
        );

        if (profileInformation == null) {
            return responses;
        }

        responses = customProblemCommentMapper
                .mappingProfileInformationToChildrenProblemComments(
                        responses,
                        profileInformation
                );

        if (requestUserUuid == null) {
            return responses;
        }

        List<UUID> commentIds = extractProblemCommentIdFrom2DArrayProblemComments(
                responses.getContent()
        );

        List<ProblemCommentReaction> reactions = problemCommentReactionRepository.findAllByCommentIdsAndUserUuid(
                commentIds,
                requestUserUuid
        );

        return mappingReactionToPageProblemComments(
                responses,
                reactions
        );

    }

    private List<String> extractUserUidFrom2DArrayProblemComments(
            List<DetailsProblemCommentResponse> problemComments) {
        Set<String> userUids = new HashSet<>(); // use set to remove duplicate user uid

        for (DetailsProblemCommentResponse problemComment : problemComments) {
            userUids.add(problemComment.getUserUid());
            if (problemComment.getChildrenComments() != null) {
                // recursive
                /*userUids.addAll(
                        extractUserUidFrom2DArrayProblemComments(
                                problemComment.getChildrenComments().getContent()
                        )
                );*/

                // 2d array
                for (DetailsProblemCommentResponse childComment : problemComment.getChildrenComments().getContent()) {
                    userUids.add(childComment.getUserUid());
                }
            }
        }

        return List.copyOf(userUids);
    }

    private List<UUID> extractProblemCommentIdFrom2DArrayProblemComments(
            List<DetailsProblemCommentResponse> problemComments) {
        List<UUID> commentIds = new ArrayList<>(); // don't need to use set because comment id is unique

        for (DetailsProblemCommentResponse problemComment : problemComments) {
            commentIds.add(problemComment.getCommentId());
            if (problemComment.getChildrenComments() != null) {
                // 2d array
                for (DetailsProblemCommentResponse childComment : problemComment.getChildrenComments().getContent()) {
                    commentIds.add(childComment.getCommentId());
                }
            }
        }

        return commentIds;
    }

    private List<String> extractUserUidFrom1DArrayProblemComments(
            List<DetailsProblemCommentResponse> problemComments) {
        Set<String> userUids = new HashSet<>();

        for (DetailsProblemCommentResponse problemComment : problemComments) {
            userUids.add(problemComment.getUserUid());
        }

        return List.copyOf(userUids);
    }

    public Integer upvoteProblemComment(String userUid, UUID commentId) {
        ProblemComment problemComment = problemCommentRepository.findById(
                commentId
        ).orElseThrow(
                () -> new AppException(ErrorCode.COMMENT_NOT_EXIST)
        );
        UUID userUuid = ParseUUID.normalizeUID(userUid);

        Boolean isReactionExisted = problemCommentReactionRepository.existsByProblemComment_CommentIdAndReactionId_UserUuid(
                commentId,
                userUuid
        );

        if (isReactionExisted) {
            throw new AppException(ErrorCode.REACTION_EXISTED);
        }

        ProblemCommentReactionId id = ProblemCommentReactionId
                .builder()
                .commentId(commentId)
                .userUuid(userUuid)
                .build();

        ProblemCommentReaction reaction = ProblemCommentReaction
                .builder()
                .reactionId(id)
                .problemComment(problemComment)
                .build();

        reaction = problemCommentReactionRepository.save(reaction);

        problemComment.getReactions().add(reaction);

        problemComment.setNumberOfLikes(
               (problemComment.getReactions().size())
        );// (long)

        problemComment = problemCommentRepository.save(problemComment);
        try{
            NotificationRequest notificationRequest = new NotificationRequest();
            String userName = identityClient.getSingleProfileInformation(
                    new SingleProfileInformationRequest(userUid))
                    .block().getResult().getDisplayName();
            notificationRequest.setTitle(userName + " has been upvoted your comment");
            notificationRequest.setMessage("");
            identityClient.postNotifications(notificationRequest);
        }catch (Exception ignored){}

        return problemComment.getReactions().size();
    }

    @Transactional
    public Integer removeUpvoteProblemComment(UUID userUuid, UUID commentId) {
        ProblemComment problemComment = problemCommentRepository.findById(
                commentId
        ).orElseThrow(
                () -> new AppException(ErrorCode.COMMENT_NOT_EXIST)
        );

        /*problemCommentReactionRepository.deleteByProblemComment_CommentIdAndReactionId_UserUuid(
                commentId,
                userUuid
        );
        problemCommentReactionRepository.flush();*/

        ProblemCommentReaction reaction = problemCommentReactionRepository
                .findByProblemComment_CommentIdAndReactionId_UserUuid(commentId, userUuid)
                .orElseThrow(
                        () -> new AppException(ErrorCode.REACTION_NOT_EXISTED)
                );


       /* problemComment = problemCommentRepository.findById(
                commentId
        ).orElseThrow(
                () -> new AppException(ErrorCode.COMMENT_NOT_EXIST)
        );*/



        problemComment.getReactions().remove(reaction);

        problemComment.setNumberOfLikes(
                (problemComment.getReactions().size())
        ); //(long)

        problemComment = problemCommentRepository.save(problemComment);

        problemCommentReactionRepository.delete(reaction);

        return problemComment.getReactions().size();
    }

    public List<ProblemCommentReaction> getListProblemCommentReactionByListCommentAndUserUuid(List<UUID> commentIds, UUID userUuid) {
        return problemCommentReactionRepository.findAllByCommentIdsAndUserUuid(
                commentIds,
                userUuid
        );
    }

    public Page<DetailsProblemCommentResponse> mappingReactionToPageProblemComments(
            Page<DetailsProblemCommentResponse> problemComments,
            List<ProblemCommentReaction> reactions
    ) {
        Map<UUID, ProblemCommentReaction> reactionMap = new HashMap<>();

        for (ProblemCommentReaction reaction : reactions) {
            reactionMap.put(
                    reaction.getReactionId().getCommentId(),
                    reaction
            );
        }

        for (DetailsProblemCommentResponse problemComment : problemComments) {
            ProblemCommentReaction reaction = reactionMap.get(problemComment.getCommentId());

            problemComment.setIsUpVoted(reaction != null);

            if (problemComment.getChildrenComments() != null
                    && !problemComment.getChildrenComments().isEmpty()
            ) {

                for (DetailsProblemCommentResponse childComment : problemComment.getChildrenComments().getContent()) {
                    ProblemCommentReaction childReaction = reactionMap.get(childComment.getCommentId());

                    childComment.setIsUpVoted(childReaction != null);
                }
            }


        }

        return problemComments;
    }



}
