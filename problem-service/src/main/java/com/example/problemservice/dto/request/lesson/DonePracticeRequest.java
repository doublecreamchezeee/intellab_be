package com.example.problemservice.dto.request.lesson;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DonePracticeRequest {
    String problemId;
    String UserId;
}
