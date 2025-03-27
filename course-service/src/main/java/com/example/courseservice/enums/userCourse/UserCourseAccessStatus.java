package com.example.courseservice.enums.userCourse;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum UserCourseAccessStatus {
    ACCESSIBLE("accessible"),
    INACCESSIBLE("inaccessible"),
    ;

    String code;
}
