package com.example.problemservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.problemservice.dto.response.testcase.TestCaseCreationResponse;
import com.example.problemservice.model.TestCase;

@Mapper(componentModel = "spring")
public interface TestCaseMapper {
    // @Mapping(source = "problem", ignore = true)
    // @Mapping(source = "submitOutputs", ignore = true)
    // @Mapping(source = "runCodeOutputs", ignore = true)
    TestCaseCreationResponse toTestCaseResponse(TestCase testCase);
}
