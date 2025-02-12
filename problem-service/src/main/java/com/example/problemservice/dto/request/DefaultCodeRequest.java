package com.example.problemservice.dto.request;


import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DefaultCodeRequest {
    UUID problemId;
    String structure;
}
