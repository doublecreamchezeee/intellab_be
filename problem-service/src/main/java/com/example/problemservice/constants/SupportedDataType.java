package com.example.problemservice.constants;

import java.util.List;

public class SupportedDataType {
    public static final String STRING = "string";
    public static final String INTEGER = "int";
    public static final String FLOAT = "float";
    public static final String BOOLEAN = "bool";
    public static final String LIST_STRING = "list<string>";
    public static final String LIST_INTEGER = "list<int>";
    public static final String LIST_FLOAT = "list<float>";
    public static final String LIST_BOOLEAN = "list<bool>";

    public static String mapTypeToJava(String type) {
        return switch (type) {
            case INTEGER -> "int";
            case FLOAT -> "float";
            case STRING -> "String";
            case BOOLEAN -> "boolean";
            case LIST_INTEGER -> "List<Integer>";
            case LIST_FLOAT -> "List<Float>";
            case LIST_STRING -> "List<String>";
            case LIST_BOOLEAN -> "List<Boolean>";
            default -> "unknown";
        };
    }

    public static List<String> getSupportedDataTypes() {
        return List.of(STRING, INTEGER, FLOAT, BOOLEAN, LIST_STRING, LIST_INTEGER, LIST_FLOAT, LIST_BOOLEAN);
    }
}
