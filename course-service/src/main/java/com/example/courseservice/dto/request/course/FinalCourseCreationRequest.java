package com.example.courseservice.dto.request.course;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FinalCourseCreationRequest {
    float price;
    String unitPrice;
    Integer templateCode = 1;
}
