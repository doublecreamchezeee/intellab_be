package com.example.problemservice.mapper;

import com.example.problemservice.dto.request.ProblemSubmission.DetailsProblemSubmissionRequest;
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

    @Mapping(target = "programmingLanguage", ignore = true)
    @Mapping(target = "scoreAchieved", ignore = true)
    @Mapping(target = "problem", ignore = true)
    @Mapping(target = "submitOrder", ignore = true)
    @Mapping(target = "submissionId", ignore = true)
    @Mapping(target = "testCasesOutput", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "isSolved", ignore = true)
    ProblemSubmission toProblemSubmission(DetailsProblemSubmissionRequest detailsProblemSubmissionRequest);
}
