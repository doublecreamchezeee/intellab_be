package com.example.problemservice.converter;

import com.example.problemservice.dto.request.judge0.TestCaseRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;

import java.util.Map;

@AllArgsConstructor
public class TestCaseRequestConverter {
    static ObjectMapper objectMapper;

    public static Map<String, Object> convertToMap(TestCaseRequest testCaseRequest) {
        return objectMapper.convertValue(testCaseRequest, Map.class);
    }
}
