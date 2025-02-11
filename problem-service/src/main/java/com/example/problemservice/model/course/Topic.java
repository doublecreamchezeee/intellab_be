package com.example.problemservice.model.course;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Topic {
    UUID topicId;
    String title;
    String content;
    Integer numberOfLikes;
    String postReach;
    String userUid;
}
