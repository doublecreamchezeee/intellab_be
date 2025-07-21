package com.example.problemservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PolygonProblemData {
    private String title;
    private String descriptionHtml;
    private String solutionCode;
    private String solutionLanguage;
    private List<PolygonTestCase> testCases;
}
