package com.example.problemservice.mapper;

import com.example.problemservice.dto.request.solution.SolutionCreationRequest;
import com.example.problemservice.dto.request.solution.SolutionIdRequest;
import com.example.problemservice.dto.request.solution.SolutionUpdateRequest;
import com.example.problemservice.dto.response.solution.DetailsSolutionResponse;
import com.example.problemservice.dto.response.solution.SolutionCreationResponse;
import com.example.problemservice.dto.response.solution.SolutionUpdateResponse;
import com.example.problemservice.model.Solution;
import com.example.problemservice.model.composite.SolutionID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SolutionMapper {

    @Mapping(target = "problem", ignore = true)
    Solution toSolution(SolutionCreationRequest solutionCreationRequest);

    @Mapping(target = "authorId", source = "authorId")
    @Mapping(target = "problemId", source = "problemId")
    SolutionCreationResponse toSolutionCreationResponse(Solution solution);

    @Mapping(target = "problem", ignore = true)
    Solution toSolution(SolutionUpdateRequest solutionUpdateRequest);

    @Mapping(target = "authorId", source = "authorId")
    @Mapping(target = "problemId", source = "problemId")
    SolutionUpdateResponse toSolutionUpdateResponse(Solution solution);

    @Mapping(target = "authorId", source = "authorId")
    @Mapping(target = "problemId", source = "problemId")
    DetailsSolutionResponse toDetailsSolutionResponse(Solution solution);

}
