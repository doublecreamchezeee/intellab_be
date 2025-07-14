package com.example.problemservice.dto.request.judge0;

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
public class TestCaseRequest {
    String source_code;
    Integer language_id;
    String stdin;
    String expected_output;
    String callback_url;
}
