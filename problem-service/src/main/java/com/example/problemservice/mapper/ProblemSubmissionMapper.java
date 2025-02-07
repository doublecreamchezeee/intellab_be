package com.example.problemservice.mapper;

import com.example.problemservice.dto.request.ProblemSubmission.DetailsProblemSubmissionRequest;
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
    @Mapping(target = "problemId", source = "problem.problemId")
    @Mapping(target = "userUid", source = "userUid")
    DetailsProblemSubmissionResponse toDetailsProblemSubmissionResponse(ProblemSubmission problemSubmission);

    @Mapping(target = "programming_language", ignore = true)
    @Mapping(target = "score_achieved", ignore = true)
    @Mapping(target = "problem", ignore = true)
    @Mapping(target = "submit_order", ignore = true)
    @Mapping(target = "submission_id", ignore = true)
    @Mapping(target = "testCases_output", ignore = true)
    @Mapping(target = "userUid", ignore = true)
    ProblemSubmission toProblemSubmission(DetailsProblemSubmissionRequest detailsProblemSubmissionRequest);
}
