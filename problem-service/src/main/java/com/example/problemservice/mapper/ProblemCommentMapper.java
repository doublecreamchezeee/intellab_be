package com.example.problemservice.mapper;

import com.example.problemservice.dto.request.problemComment.ProblemCommentCreationRequest;
import com.example.problemservice.dto.request.problemComment.ProblemCommentUpdateRequest;
import com.example.problemservice.dto.response.problemComment.ProblemCommentCreationResponse;
import com.example.problemservice.dto.response.problemComment.ProblemCommentUpdateResponse;
import com.example.problemservice.dto.response.problemComment.SingleProblemCommentResponse;
import com.example.problemservice.model.ProblemComment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProblemCommentMapper {
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lastModifiedAt", ignore = true)
    ProblemComment toProblemComment(ProblemCommentCreationRequest problemCommentCreationRequest);

    @Mapping(target = "parentCommentId", source = "parentComment.commentId")
    @Mapping(target = "replyToCommentId", source = "repliedComment.commentId")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "lastModifiedAt", source = "lastModifiedAt")
    ProblemCommentCreationResponse toProblemCommentCreationResponse(ProblemComment problemComment);

    void updateProblemCommentFromRequest(ProblemCommentUpdateRequest request, @MappingTarget ProblemComment problemComment);

    @Mapping(target = "parentCommentId", source = "parentComment.commentId")
    @Mapping(target = "replyToCommentId", source = "repliedComment.commentId")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "lastModifiedAt", source = "lastModifiedAt")
    ProblemCommentUpdateResponse toProblemCommentUpdateResponse(ProblemComment problemComment);

    @Mapping(target = "parentCommentId", source = "parentComment.commentId")
    @Mapping(target = "replyToCommentId", source = "repliedComment.commentId")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "lastModifiedAt", source = "lastModifiedAt")
    //@Mapping(target = "isModified", expression = "java(problemComment.getLastModifiedAt().isAfter(problemComment.getCreatedAt()))")
    SingleProblemCommentResponse toSingleProblemCommentResponse(ProblemComment problemComment);
}
