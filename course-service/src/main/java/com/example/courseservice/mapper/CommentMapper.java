package com.example.courseservice.mapper;

import com.example.courseservice.client.IdentityClient;
import com.example.courseservice.dto.response.Comment.CommentResponse;
import com.example.courseservice.dto.response.profile.MultipleProfileInformationResponse;
import com.example.courseservice.dto.response.profile.SingleProfileInformationResponse;
import com.example.courseservice.model.Comment;
import com.example.courseservice.model.Firestore.User;
import com.example.courseservice.model.Reaction;
import com.example.courseservice.service.FirestoreService;
import com.example.courseservice.utils.ParseUUID;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommentMapper {

    @SneakyThrows
    public CommentResponse toResponse(Comment comment) {
        Boolean isModified = !comment.getCreated().equals(comment.getLastModified());
        //String uid = getUserUid(comment.getUserId());

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
                .userName(null)
                .userUid(null)
                .avatarUrl(null)
                .isOwner(null)
                .isUpvoted(null)
                .build();
    }

    public Page<CommentResponse> mapProfileToCommentResponsePage(
            Page<CommentResponse> commentResponses,
            MultipleProfileInformationResponse profiles)
    {
        if (commentResponses == null || commentResponses.getContent().isEmpty()) {
            return null;
        }

        Map<UUID, SingleProfileInformationResponse> profileMap = new HashMap<>();
        for (SingleProfileInformationResponse profile : profiles.getProfiles()) {
            profileMap.put(ParseUUID.normalizeUID(profile.getUserId()), profile);
        }

        for (CommentResponse comment : commentResponses.getContent()) {
            SingleProfileInformationResponse profile = profileMap.get(comment.getUserId());
            if (profile != null)
            {
                setProfileAttribute(comment, profile);
            }
            if (comment.getComments() != null && !comment.getComments().isEmpty()) {
                for (CommentResponse comment2 : comment.getComments()) {
                    SingleProfileInformationResponse profile2 = profileMap.get(comment2.getUserId());
                    if (profile2 != null){
                        setProfileAttribute(comment2, profile2);
                    }
                }
            }
        }
        return commentResponses;
    }

    public CommentResponse mapProfileToCommentResponse(
            CommentResponse commentResponse,
            MultipleProfileInformationResponse profiles)
    {
        if (commentResponse == null) {
            return null;
        }

        Map<UUID, SingleProfileInformationResponse> profileMap = new HashMap<>();

        for (SingleProfileInformationResponse profile : profiles.getProfiles()) {
            profileMap.put(ParseUUID.normalizeUID(profile.getUserId()), profile);
        }

        SingleProfileInformationResponse profile = profileMap.get(commentResponse.getUserId());

        if (profile != null)
        {
            setProfileAttribute(commentResponse, profile);
        }
        if (commentResponse.getComments() != null && !commentResponse.getComments().isEmpty()) {
            for (CommentResponse comment2 : commentResponse.getComments()) {
                SingleProfileInformationResponse profile2 = profileMap.get(comment2.getUserId());
                if (profile2 != null){
                    setProfileAttribute(comment2, profile2);
                }
            }
        }

        return commentResponse;
    }

    private void setProfileAttribute(CommentResponse comment, SingleProfileInformationResponse profile) {
        comment.setAvatarUrl(profile.getPhotoUrl());
        comment.setUserUid(profile.getUserId());
        comment.setUserName(profile.getDisplayName());
    }


}