package com.example.problemservice.dto.request.problem;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EnrichCodeRequest {
    String code;
    String structure;
    Integer languageId;
}
