package com.example.problemservice.utils;

import com.example.problemservice.core.DataField;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProblemDefinitionParser {
    String problemName = "";
    String functionName = "";
    List<DataField> inputDataFields = new ArrayList<>();
    List<DataField> outputDataFields = new ArrayList<>();

    public void parse(String input) {
        String[] lines = input.split("\n");
        String currentSection = null;

        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("Problem Name:")) {
                this.problemName = extractQuotedValue(line);
            } else if (line.startsWith("Function Name:")) {
                this.functionName = extractValue(line);
            } else if (line.startsWith("Input Structure:")) {
                currentSection = "input";
            } else if (line.startsWith("Output Structure:")) {
                currentSection = "output";
            } else if (line.startsWith("Input DataField:")) {
                if ("input".equals(currentSection)) {
                    DataField dataField = extractFieldIO(line);
                    if (dataField != null) this.inputDataFields.add(dataField);
                }
            } else if (line.startsWith("Output DataField:")) {
                if ("output".equals(currentSection)) {
                    DataField dataField = extractFieldIO(line);
                    if (dataField != null) this.outputDataFields.add(dataField);
                }
            }
        }
    }

    private String extractQuotedValue(String line) {
        return line.replaceAll(".*: \"(.*)\"$", "$1");
    }

    private String extractValue(String line) {
        return line.replaceAll(".*: (\\w+)$", "$1");
    }

    private DataField extractFieldIO(String line) {
        String[] parts = line.split(" ");
        if (parts.length == 3) {
            return new DataField(parts[1], parts[2]);
        }
        return null;
    }

    public String generateCpp() {
        String inputs = inputDataFields.stream()
                .map(dataField -> mapTypeToCpp(dataField.type) + " " + dataField.name)
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
        return mapTypeToCpp(outputDataFields.get(0).type) + " " + functionName + "(" + inputs + ") {\n    // Implementation goes here\n    return result;\n}";
    }

    public String generateJs() {
        String inputs = inputDataFields.stream()
                .map(dataField -> dataField.name)
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
        return "function " + functionName + "(" + inputs + ") {\n    // Implementation goes here\n    return result;\n}";
    }

    public String generateRust() {
        String inputs = inputDataFields.stream()
                .map(dataField -> dataField.name + ": " + mapTypeToRust(dataField.type))
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
        String outputType = mapTypeToRust(outputDataFields.get(0).type);
        return "fn " + functionName + "(" + inputs + ") -> " + outputType + " {\n    // Implementation goes here\n    result\n}";
    }

    public String generateJava() {
        String inputs = inputDataFields.stream()
                .map(dataField -> mapTypeToJava(dataField.type) + " " + dataField.name)
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
        return "public static " + mapTypeToJava(outputDataFields.get(0).type) + " " + functionName + "(" + inputs + ") {\n    // Implementation goes here\n    return result;\n}";
    }

    private String mapTypeToRust(String type) {
        return switch (type) {
            case "int" -> "i32";
            case "float" -> "f64";
            case "string" -> "String";
            case "bool" -> "bool";
            case "list<int>" -> "Vec<i32>";
            case "list<float>" -> "Vec<f64>";
            case "list<string>" -> "Vec<String>";
            case "list<bool>" -> "Vec<bool>";
            default -> "unknown";
        };
    }

    private String mapTypeToCpp(String type) {
        return switch (type) {
            case "int" -> "int";
            case "float" -> "float";
            case "string" -> "std::string";
            case "bool" -> "bool";
            case "list<int>" -> "std::vector<int>";
            case "list<float>" -> "std::vector<float>";
            case "list<string>" -> "std::vector<std::string>";
            case "list<bool>" -> "std::vector<bool>";
            default -> "unknown";
        };
    }

    private String mapTypeToJava(String type) {
        return switch (type) {
            case "int" -> "int";
            case "float" -> "float";
            case "string" -> "String";
            case "bool" -> "boolean";
            case "list<int>" -> "List<Integer>";
            case "list<float>" -> "List<Float>";
            case "list<string>" -> "List<String>";
            case "list<bool>" -> "List<Boolean>";
            default -> "unknown";
        };
    }

}