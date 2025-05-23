package com.example.problemservice.mapper;

import com.example.problemservice.dto.request.problem.ProblemCreationRequest;
import com.example.problemservice.dto.response.Problem.DetailsProblemResponse;
import com.example.problemservice.dto.response.Problem.ProblemCreationResponse;
import com.example.problemservice.dto.response.Problem.ProblemRowResponse;
import com.example.problemservice.model.Problem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProblemMapper {

    @Mapping(target = "problemId",source = "problemId")
    @Mapping(target = "problemName", source = "problemName")
    @Mapping(target = "level", source = "problemLevel")
    @Mapping(target = "acceptanceRate", source = "acceptanceRate")
    @Mapping(target = "hintCount", expression = "java(problem.getHints().size())")
    @Mapping(target = "isDone", ignore = true)
    @Mapping(target = "categories", ignore = true)
    @Mapping(target = "isPublished", source = "isPublished")
    @Mapping(target = "hasSolution", expression = "java(problem.getSolution() != null)")
    ProblemRowResponse toProblemRowResponse(Problem problem);

    @Mapping(target = "problemStructure", ignore = true)
    @Mapping(target = "categories", ignore = true)
    Problem toProblem(ProblemCreationRequest problemCreationRequest);

    @Mapping(target = "hasSolution", expression = "java(problem.getSolution() != null)")
    @Mapping(target = "problemStructure", ignore = true)
    ProblemCreationResponse toProblemCreationResponse(Problem problem);

    @Mapping(target = "hasSolution", expression = "java(problem.getSolution() != null)")
    DetailsProblemResponse toProblemDetailsResponse(Problem problem);

    @Mapping(target = "problemStructure", ignore = true)
    @Mapping(target = "categories", ignore = true)
    void updateProblemFromRequest(ProblemCreationRequest request, @MappingTarget Problem problem);
}
