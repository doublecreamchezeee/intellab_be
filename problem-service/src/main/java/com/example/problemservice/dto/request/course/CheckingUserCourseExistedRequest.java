package com.example.problemservice.dto.request.course;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CheckingUserCourseExistedRequest {
    UUID problemId;
    UUID userUuid;
}
