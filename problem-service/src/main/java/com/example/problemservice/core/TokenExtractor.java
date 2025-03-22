package com.example.problemservice.core;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TokenExtractor {
    public static List<String> extractTokens(String jsonArray) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            List<Map<String, String>> list = objectMapper.readValue(jsonArray, new TypeReference<List<Map<String, String>>>() {});
            return list.stream()
                    .map(map -> map.get("token"))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract tokens", e);
        }
    }
}
