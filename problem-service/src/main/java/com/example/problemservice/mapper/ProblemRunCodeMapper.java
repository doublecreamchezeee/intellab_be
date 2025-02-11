package com.example.problemservice.mapper;

import com.example.problemservice.dto.request.problemRunCode.DetailsProblemRunCodeRequest;
import com.example.problemservice.dto.response.problemRunCode.CreationProblemRunCodeResponse;
import com.example.problemservice.dto.response.problemRunCode.DetailsProblemRunCodeResponse;
import com.example.problemservice.model.ProblemRunCode;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProblemRunCodeMapper {
    @Mapping(target = "runCodeId", source = "runCodeId")
    @Mapping(target = "code", source = "code")
    @Mapping(target = "programmingLanguage", source = "programmingLanguage")
    @Mapping(target = "problemId", source = "problem.problemId")
    @Mapping(target = "userUid", source = "userId")
    DetailsProblemRunCodeResponse toDetailsProblemRunCodeResponse(ProblemRunCode problemRunCode);

    @Mapping(target = "runCodeId", ignore = true)
    @Mapping(target = "programmingLanguage", ignore = true)
    @Mapping(target = "problem", ignore = true)
    @Mapping(target = "testCasesRunCodeOutput", ignore = true)
    @Mapping(target = "userId", ignore = true)
    ProblemRunCode toProblemRunCode(DetailsProblemRunCodeRequest detailsProblemRunCodeRequest);

    @Mapping(target = "runCodeId", source = "runCodeId")
    @Mapping(target = "code", source = "code")
    @Mapping(target = "programmingLanguage", source = "programmingLanguage")
    @Mapping(target = "problemId", source = "problem.problemId")
    @Mapping(target = "userUid", source = "userId")
    CreationProblemRunCodeResponse toCreationProblemRunCodeResponse(ProblemRunCode problemRunCode);


}
