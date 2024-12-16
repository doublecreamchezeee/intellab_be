package com.example.problemservice.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Judge0Response {
    String token;
    String stdout;
    String stderr;
    StatusResponse status;
}
