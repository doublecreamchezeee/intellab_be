package com.example.problemservice.dto.response.problemRunCode;

import com.example.problemservice.dto.response.testcase.DetailsTestCaseRunCodeOutput;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DetailsProblemRunCodeResponse {
    UUID runCodeId;
    String code;
    String programmingLanguage;
    UUID problemId;
    UUID userUid;
    List<DetailsTestCaseRunCodeOutput> testcases;
}
