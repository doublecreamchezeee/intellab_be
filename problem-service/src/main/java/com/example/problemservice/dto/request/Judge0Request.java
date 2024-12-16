package com.example.problemservice.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Judge0Request {
    private String sourceCode;
    private int languageId;
    private String stdin;
}
