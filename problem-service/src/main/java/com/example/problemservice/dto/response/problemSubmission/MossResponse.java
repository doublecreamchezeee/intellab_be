package com.example.problemservice.dto.response.problemSubmission;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MossResponse {
    List<MossMatchResponse> results;
}
