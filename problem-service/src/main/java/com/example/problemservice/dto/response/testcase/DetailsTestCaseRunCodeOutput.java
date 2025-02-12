package com.example.problemservice.dto.response.testcase;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DetailsTestCaseRunCodeOutput {
    String input;
    String expectedOutput;
    String actualOutput;
    String status;
    Integer statusId;
    String message;
    String time;
    String memoryUsage;
    String error;
    String compileOutput;
}
