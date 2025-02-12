package com.example.problemservice.dto.response.problemRunCode;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreationProblemRunCodeResponse {
    UUID runCodeId;
    String code;
    String programmingLanguage;
    UUID problemId;
    UUID userUid;
}
