package com.example.problemservice.dto.request.testcase;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class TestCasesGenerationRequest<T> {
    int numberOfTestCases;
    int minArrayLength;
    int maxArrayLength;
    T minValue;
    T maxValue;
    String directoryPath;
    Class<T> clazz;
}

