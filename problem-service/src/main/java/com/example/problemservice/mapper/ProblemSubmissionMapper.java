package com.example.problemservice.mapper;

import com.example.problemservice.dto.response.problemSubmission.DetailsProblemSubmissionResponse;
import com.example.problemservice.model.ProblemSubmission;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProblemSubmissionMapper {
    @Mapping(target = "submissionId", source = "submission_id")
    @Mapping(target = "submissionOrder", source = "submit_order")
    @Mapping(target = "code", source = "code")
    @Mapping(target = "programmingLanguage", source = "programming_language")
    @Mapping(target = "scoreAchieved", source = "score_achieved")
    @Mapping(target = "problemId", source = "problem.problem_id")
    @Mapping(target = "userUid", source = "userUid")
    DetailsProblemSubmissionResponse toDetailsProblemSubmissionResponse(ProblemSubmission problemSubmission);
}
