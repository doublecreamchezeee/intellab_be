package com.example.courseservice.dto.request.lesson;


import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateOrderRequest {
    UUID courseId;
    List<UUID> lessonIds;
}
