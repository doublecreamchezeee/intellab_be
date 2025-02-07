package com.example.problemservice.dto.response.DefaultCode;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PartialBoilerplateResponse {
    Integer languageId;
    String code;
    String longName;
    String shortName;
}
