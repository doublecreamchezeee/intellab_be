package com.example.problemservice.mapper;


import com.example.problemservice.dto.response.testcase.DetailsTestCaseRunCodeOutput;
import com.example.problemservice.model.TestCaseRunCodeOutput;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TestCaseRunCodeOutputMapper {
    @Mapping(target = "input", source = "testcase.input")
    @Mapping(target = "expectedOutput", source = "testcase.output")
    @Mapping(target = "actualOutput", source = "submissionOutput")
    @Mapping(target = "status", source = "resultStatus")
    @Mapping(target = "message", source = "message")
    @Mapping(target = "time", source = "runtime")
    @Mapping(target = "memoryUsage", source = "memoryUsage")
    @Mapping(target = "error", source = "error")
    @Mapping(target = "compileOutput", source = "compileOutput")
    DetailsTestCaseRunCodeOutput toDetailsTestCaseRunCodeOutput(TestCaseRunCodeOutput testCaseRunCodeOutput);
}
