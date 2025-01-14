package com.example.problemservice.mapper;

import com.example.problemservice.dto.response.Problem.CategoryRessponse;
import com.example.problemservice.dto.response.Problem.ProblemRowResponse;
import com.example.problemservice.model.Problem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface ProblemMapper {

    @Mapping(target = "problemId",source = "problemId")
    @Mapping(target = "problemName", source = "problemName")
    @Mapping(target = "level", source = "problemLevel")
    @Mapping(target = "acceptanceRate", source = "acceptanceRate")
    @Mapping(target = "hintCount", expression = "java(problem.getHints().size())")
    ProblemRowResponse toProblemRowResponse(Problem problem);
}


//ProblemRowResponse {
//    UUID problemId;
//    String problemName;
//    String level;
//    boolean isDone;
//    Float acceptanceRate;
//    List<CategoryRessponse> categories;
//}