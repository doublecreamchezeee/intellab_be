package com.example.courseservice.dto.request.exercise;


import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ModifyQuesstionListRequest {
    List<UUID> addQuestions;
    List<UUID> removeQuestions;
}
