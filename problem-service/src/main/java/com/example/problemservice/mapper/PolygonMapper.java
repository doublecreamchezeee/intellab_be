package com.example.problemservice.mapper;

import com.example.problemservice.core.ProblemStructure;
import com.example.problemservice.dto.PolygonProblemData;
import com.example.problemservice.dto.PolygonTestCase;
import com.example.problemservice.dto.request.TestCaseCreationRequest;
import com.example.problemservice.dto.request.problem.ProblemCreationRequest;
import com.example.problemservice.dto.request.solution.SolutionCreationRequest;
import com.example.problemservice.dto.request.testcase.TestCaseMultipleCreationRequest;
import com.example.problemservice.model.Problem;
import com.example.problemservice.model.Solution;
import com.example.problemservice.model.composite.SolutionID;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PolygonMapper {


    public static ProblemCreationRequest toProblemCreationRequest(PolygonProblemData data) {
        ProblemCreationRequest request = new ProblemCreationRequest();
        request.setProblemName(data.getTitle());
        return request;
    }

    public static TestCaseMultipleCreationRequest toTestCases(PolygonProblemData data, String problemId) {
        List<Integer> orders = new ArrayList<>();
        List<String> inputs = new ArrayList<>();
        List<String> outputs = new ArrayList<>();

        int index = 1;
        for (PolygonTestCase tc : data.getTestCases()) {
            orders.add(index++);
            inputs.add(tc.getInput());
            outputs.add(tc.getOutput());
        }

        return TestCaseMultipleCreationRequest.builder()
                .problemId(UUID.fromString(problemId))
                .orders(orders)
                .inputs(inputs)
                .outputs(outputs)
                .build();
    }


    public static SolutionCreationRequest toSolutionCreationRequest(PolygonProblemData data, String problemId, String authorId) {
        return SolutionCreationRequest.builder()
                .problemId(problemId)
                .authorId(authorId)
                .content(data.getSolutionCode()) // Assuming `getSolution()` returns a `String`
                .build();
    }

}
