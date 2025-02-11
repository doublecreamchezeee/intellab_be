package com.example.problemservice.dto.request.problemRunCode;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DetailsProblemRunCodeRequest {
    String code;
    Integer languageId;
    UUID problemId;
}
