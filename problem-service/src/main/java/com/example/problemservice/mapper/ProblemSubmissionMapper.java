package com.example.problemservice.mapper;

import com.example.problemservice.dto.response.problemSubmission.DetailsProblemSubmissionResponse;
import com.example.problemservice.model.ProblemSubmission;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProblemSubmissionMapper {
    @Mapping(target = "submissionId", source = "submissionId")
    @Mapping(target = "submissionOrder", source = "submitOrder")
    @Mapping(target = "code", source = "code")
    @Mapping(target = "programmingLanguage", source = "programmingLanguage")
    @Mapping(target = "scoreAchieved", source = "scoreAchieved")
    @Mapping(target = "problemId", source = "problem.problemId")
    @Mapping(target = "userUid", source = "userId")
    DetailsProblemSubmissionResponse toDetailsProblemSubmissionResponse(ProblemSubmission problemSubmission);
}
