package com.example.identityservice.dto.response.course;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CompleteCourseResponse {
    Object course;
    Float progressPercent;
    // Done, Learning, Expired
    String status;
    Instant lastAccessedDate;
    UUID certificateId;
    Date completedDate;
}
