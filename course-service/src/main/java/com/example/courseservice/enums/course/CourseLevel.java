package com.example.courseservice.enums.course;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = lombok.AccessLevel.PRIVATE)
public enum CourseLevel {
    Beginner("Beginner"),
    Intermediate("Intermediate"),
    Advanced("Advanced"),
    ;
    String code;

}
