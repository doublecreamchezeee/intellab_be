package com.example.problemservice.enums;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum TestCaseDataType {
    INTEGER("INTEGER", Integer.class),
    LONG("LONG", Long.class),
    DOUBLE("DOUBLE", Double.class),
    ;
    String code;
    Class clazz;
}
