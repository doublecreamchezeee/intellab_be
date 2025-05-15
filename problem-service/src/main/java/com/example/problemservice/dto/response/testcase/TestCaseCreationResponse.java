package com.example.problemservice.dto.response.testcase;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TestCaseCreationResponse {
  String testcaseId;
  String input;
  String output;
  Integer order;
}
