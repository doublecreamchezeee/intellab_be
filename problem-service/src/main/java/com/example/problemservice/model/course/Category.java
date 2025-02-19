package com.example.problemservice.model.course;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Setter
@Getter
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Category {
    Integer categoryId;
    String name;
}
