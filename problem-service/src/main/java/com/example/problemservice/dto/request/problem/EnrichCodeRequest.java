package com.example.problemservice.dto.request.problem;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EnrichCodeRequest {
    String code;
    UUID problemId;
    Integer languageId;
}
