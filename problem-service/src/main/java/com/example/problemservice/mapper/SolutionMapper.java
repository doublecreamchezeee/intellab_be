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

    @Mapping(target = "id", ignore = true) // handled manually in the service
    @Mapping(target = "problem", ignore = true)
    Solution toSolution(SolutionCreationRequest solutionCreationRequest);

    // Entity → Response
    @Mapping(target = "problemId", source = "id.problemId")
    @Mapping(target = "authorId", source = "id.authorId")
    SolutionCreationResponse toSolutionCreationResponse(Solution solution);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "problem", ignore = true)
    Solution toSolution(SolutionUpdateRequest solutionUpdateRequest);

    @Mapping(target = "problemId", source = "id.problemId")
    @Mapping(target = "authorId", source = "id.authorId")
    SolutionUpdateResponse toSolutionUpdateResponse(Solution solution);

    @Mapping(target = "problemId", source = "id.problemId")
    @Mapping(target = "authorId", source = "id.authorId")
    DetailsSolutionResponse toDetailsSolutionResponse(Solution solution);

}
