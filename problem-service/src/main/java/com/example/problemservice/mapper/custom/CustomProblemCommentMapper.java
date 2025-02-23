package com.example.problemservice.mapper.custom;

import com.example.problemservice.dto.response.problemComment.DetailsProblemCommentResponse;
import com.example.problemservice.dto.response.profile.MultipleProfileInformationResponse;
import com.example.problemservice.dto.response.profile.SingleProfileInformationResponse;
import com.example.problemservice.model.ProblemComment;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CustomProblemCommentMapper {
    @SneakyThrows
    public DetailsProblemCommentResponse toDetailsProblemCommentResponse(
            ProblemComment problemComment,
            Pageable pageable
    ) {

        if (problemComment == null) {
            return null;
        }

        List<DetailsProblemCommentResponse> childrenProblemComments = null;

        List<ProblemComment> paginatedChildren = null;

        if (problemComment.getChildrenComments() != null
                && !problemComment.getChildrenComments().isEmpty()
        ) {
            List<ProblemComment> childrenComments = problemComment.getChildrenComments();

            // Sort childrenComments by the criteria specified in the Pageable object
            Sort sort = pageable.getSort();
            if (sort.isSorted()) {
                Comparator<ProblemComment> comparator = sort.stream()
                        .map(order -> {
                            Comparator<ProblemComment> comp;
                            switch (order.getProperty()) {
                                case "createdAt":
                                    comp = Comparator.comparing(ProblemComment::getCreatedAt);
                                    break;
                                case "lastModifiedAt":
                                    comp = Comparator.comparing(ProblemComment::getLastModifiedAt);
                                    break;
                                case "numberOfLikes":
                                    comp = Comparator.comparing(ProblemComment::getNumberOfLikes);
                                    break;
                                case "content":
                                    comp = Comparator.comparing(ProblemComment::getContent);
                                    break;
                                case "userUid":
                                    comp = Comparator.comparing(ProblemComment::getUserUid);
                                    break;
                                case "userUuid":
                                    comp = Comparator.comparing(ProblemComment::getUserUuid);
                                    break;
                                case "commentId":
                                    comp = Comparator.comparing(ProblemComment::getCommentId);
                                    break;
                                default:
                                    throw new IllegalArgumentException("Unknown property to sort in custom mapper: " + order.getProperty());
                            }
                            return order.isAscending() ? comp : comp.reversed();
                        })
                        .reduce(Comparator::thenComparing)
                        .orElseThrow(IllegalArgumentException::new);

                childrenComments.sort(comparator);
            }

            // Apply pagination
            int totalChildren = childrenComments.size();
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), totalChildren);
            paginatedChildren = childrenComments.subList(start, end);

            childrenProblemComments = paginatedChildren
                    .stream()
                    .map(
                            comment -> toDetailsProblemCommentResponse(
                                    comment,
                                    pageable
                            )
                    )
                    .toList();
        }

        //Long upvote = (long) problemComment.getReactions().size();

        return DetailsProblemCommentResponse.builder()
                .commentId(problemComment.getCommentId())
                .content(problemComment.getContent())
                .numberOfLikes(Long.valueOf(problemComment.getNumberOfLikes()))
                .problemId(problemComment.getProblem().getProblemId())
                .userUid(problemComment.getUserUid())
                .userUuid(problemComment.getUserUuid())
                .parentCommentId(
                        problemComment.getParentComment() != null
                            ?  problemComment.getParentComment().getCommentId()
                            : null
                )
                .replyToCommentId(
                        problemComment.getRepliedComment() != null
                            ? problemComment.getRepliedComment().getCommentId()
                            : null
                )
                .isModified(
                        problemComment.getIsModified()
                        //problemComment.getCreatedAt().compareTo(problemComment.getLastModifiedAt()) != 0
                )
                .createdAt(problemComment.getCreatedAt())
                .lastModifiedAt(problemComment.getLastModifiedAt())
                .username(null)
                .userAvatar(null)
                .userEmail(null)
                .isUpVoted(false)
                .childrenComments(
                        childrenProblemComments != null
                                && !childrenProblemComments.isEmpty() // don't need to check paginatedChildren
                            ? new PageImpl<>(childrenProblemComments, pageable, problemComment.getChildrenComments().size())
                            : null
                )
                .build();
    }

    public Page<DetailsProblemCommentResponse> mappingProfileInformationToChildrenProblemComments(
            Page<DetailsProblemCommentResponse> problemComments,
            MultipleProfileInformationResponse profileInformation
    ) {

        Map<String, SingleProfileInformationResponse> profileMap = new HashMap<>();

        for (SingleProfileInformationResponse profile : profileInformation.getProfiles()) {
            profileMap.put(profile.getUserId(), profile);
        }

        for (DetailsProblemCommentResponse response : problemComments.getContent()) {
            SingleProfileInformationResponse profile = profileMap.get(response.getUserUid());

            if (profile != null) {
                setProfileData(response, profile);
            }
        }

        return problemComments;
    }

    public DetailsProblemCommentResponse mappingProfileInformationToOneProblemComments(
            DetailsProblemCommentResponse problemComment,
            MultipleProfileInformationResponse profileInformation
    ) {

        Map<String, SingleProfileInformationResponse> profileMap = new HashMap<>();

        for (SingleProfileInformationResponse profile : profileInformation.getProfiles()) {
            profileMap.put(profile.getUserId(), profile);
        }

        if (problemComment.getChildrenComments() != null
                && !problemComment.getChildrenComments().isEmpty()
        ) {

            for (DetailsProblemCommentResponse response : problemComment.getChildrenComments().getContent()) {
                SingleProfileInformationResponse profile = profileMap.get(response.getUserUid());
                if (profile != null) {
                    setProfileData(response, profile);
                }
            }

        }

        SingleProfileInformationResponse profile = profileMap.get(problemComment.getUserUid());

        if (profile != null) {
            setProfileData(problemComment, profile);
        }

        return problemComment;
    }

    public Page<DetailsProblemCommentResponse> mappingProfileInformationToPageProblemComments(
            Page<DetailsProblemCommentResponse> problemComments,
            MultipleProfileInformationResponse profileInformation
    ) {

        Map<String, SingleProfileInformationResponse> profileMap = new HashMap<>();

        for (SingleProfileInformationResponse profile : profileInformation.getProfiles()) {
            profileMap.put(profile.getUserId(), profile);
        }

        for (DetailsProblemCommentResponse response : problemComments.getContent()) {
            SingleProfileInformationResponse profile = profileMap.get(response.getUserUid());

            if (profile != null) {
                setProfileData(response, profile);
            }

            if (response.getChildrenComments() != null
                    && !response.getChildrenComments().isEmpty()
            ) {
                for (DetailsProblemCommentResponse childComment : response.getChildrenComments().getContent()) {
                    SingleProfileInformationResponse childProfile = profileMap.get(childComment.getUserUid());

                    if (childProfile != null) {
                        setProfileData(childComment, childProfile);
                    }
                }
            }
        }

        return problemComments;
    }

    private void setProfileData(
            DetailsProblemCommentResponse response,
            SingleProfileInformationResponse profile
    ) {
        response.setUsername(profile.getDisplayName());
        response.setUserEmail(profile.getEmail());
        response.setUserAvatar(profile.getPhotoUrl());
    }

}
