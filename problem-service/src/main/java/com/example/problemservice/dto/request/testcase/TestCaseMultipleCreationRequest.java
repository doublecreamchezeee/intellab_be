package com.example.problemservice.dto.request.testcase;


import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TestCaseMultipleCreationRequest {
    UUID problemId;
    UUID userId;
    List<String> inputs;
    List<String> outputs;
}
