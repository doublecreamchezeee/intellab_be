package com.example.courseservice.dto.response.course;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CertificateResponse {
    CourseShortResponse course;
    Instant completeDate;
    String username;
    String certificateLink;
}
