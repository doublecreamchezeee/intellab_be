package com.example.problemservice.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class BoilerplateClient {

    @Component
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public class FullProblemDefinitionParser{
        private String problemName;
        private String functionName;
        private List<Field> inputFields;
        private List<Field> outputFields;

        public void parse(String structure){
            String[] lines = structure.split("\n");
            String currentSection = null;

            for (String rawLine : lines){
                String line = rawLine.trim();

                if (line.startsWith("Problem Name:")){
                    this.problemName = extractQuoteValue(line);
                } else if (line.startsWith("Function:")) {
                    this.functionName = extractValue(line);
                } else if (line.startsWith("Inputs Structure:")   ) {
                    currentSection = "input";
                } else if (line.startsWith("Outputs Structure:")   ) {
                    currentSection = "output";
                }
                else if (line.startsWith("Input Field:") && "input".equals(currentSection)) {
                    Field field = extractField(line);
                    if (field != null) inputFields.add(field);
                } else if (line.startsWith("Output Field:") && "output".equals(currentSection)) {
                    Field field = extractField(line);
                    if (field != null) outputFields.add(field);
                }
            }
        }

        private String extractQuoteValue(String line){
            String regex = ": \\\"(.*)\\\"$";
            return line.replaceAll(regex, "S1");
        }

        private String extractValue(String line) {
            String regex = ": (\\w+)$";
            return line.replaceAll(regex, "$1");
        }

        private Field extractField(String line) {
            String regex = "Field: (\\w+(?:<\\w+>)?) (\\w+)$";
            String[] matches = line.split(" ");
            if (matches.length >= 3) {
                return new Field(matches[1], matches[2]);
            }
            return null;
        }

        public String generateCpp()
        {
            String inputs = inputFields.stream()
                    .map(field -> mapTypeToCpp(field.getType()) + " " + field.getName())
                    .collect(Collectors.joining(", "));
            String inputReads = inputFields.stream()
                    .map(field -> {
                        if (field.getType().startsWith("list<")) {
                            return "int size_" + field.getName() + ";\n  std::cin >> size_" + field.getName() + ";\n  " +
                                    mapTypeToCpp(field.getType()) + " "
                                    + field.getName() + "(size_" + field.getName() + ");" +
                                    "\n  for(int i = 0; i < size_" + field.getName() + "; ++i) std::cin >> " + field.getName() + "[i];";
                        } else {
                            return "std::cin >> " + field.getName() + ";";
                        }
                    }).collect(Collectors.joining("\n "));
            String outputType = mapTypeToCpp(outputFields.get(0).getType());
            String functionCall = outputType + " result = " + functionName + "(" +
                    inputFields.stream().map(Field::getName).collect(Collectors.joining(", ")) + ");";
            String outputWrite = "std::cout << result << std::endl;";

            return """
#include <iostream>
#include <vector>
#include <string>

##USER_CODE_HERE##

int main() {
  %s
  %s
  %s
  return 0;
}
        """.formatted(inputReads, functionCall, outputWrite);
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


        public String generateJava() {
            String inputReads = inputFields.stream()
                    .map(field -> {
                        if (field.getType().startsWith("list<")) {
                            return """
int size_%1$s = scanner.nextInt();
List<%2$s> %1$s = new ArrayList<>();
for (int i = 0; i < size_%1$s; i++) {
    %1$s.add(scanner.next%3$s());
}
""".formatted(field.getName(), mapTypeToJava(field.getType()), mapScannerMethodForJava(field.getType()));
                        } else {
                            return "%s %s = scanner.next%s();".formatted(mapTypeToJava(field.getType()), field.getName(), mapScannerMethodForJava(field.getType()));
                        }
                    }).collect(Collectors.joining("\n  "));

            String functionCall = "%s result = %s(%s);".formatted(
                    mapTypeToJava(outputFields.get(0).getType()),
                    functionName,
                    inputFields.stream().map(Field::getName).collect(Collectors.joining(", "))
            );
            String outputWrite = "System.out.println(result);";

            return """
import java.util.*;

##USER_CODE_HERE##

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        %s
        %s
        %s
    }
}
        """.formatted(inputReads, functionCall, outputWrite);
        }

        private String mapTypeToJava(String type) {
            return switch (type) {
                case "int" -> "int";
                case "float" -> "double"; // Java uses double for floating-point numbers by default
                case "string" -> "String";
                case "bool" -> "boolean";
                case "list<int>" -> "List<Integer>";
                case "list<float>" -> "List<Double>";
                case "list<string>" -> "List<String>";
                case "list<bool>" -> "List<Boolean>";
                default -> "Object"; // Fallback for unknown types
            };
        }

        private String mapScannerMethodForJava(String type) {
            return switch (type) {
                case "int", "list<int>" -> "Int";
                case "float", "list<float>" -> "Double";
                case "string", "list<string>" -> "Line"; // Use `nextLine` for strings
                case "bool", "list<bool>" -> "Boolean"; // Note: Boolean may need additional parsing
                default -> "Object"; // Unknown or unsupported types
            };
        }

        public String generatePython() {
            String inputReads = inputFields.stream()
                    .map(field -> {
                        if (field.getType().startsWith("list<")) {
                            return """
size_%1$s = int(input())
%1$s = list(map(%2$s, input().split()[:size_%1$s]))
""".formatted(field.getName(), mapTypeToPython(field.getType()));
                        } else {
                            return "%s = %s(input())".formatted(field.getName(), mapTypeToPython(field.getType()));
                        }
                    }).collect(Collectors.joining("\n"));

            String functionCall = "result = %s(%s)".formatted(
                    functionName,
                    inputFields.stream().map(Field::getName).collect(Collectors.joining(", "))
            );
            String outputWrite = "print(result)";

            return """
##USER_CODE_HERE##

if __name__ == "__main__":
    %s
    %s
    %s
        """.formatted(inputReads, functionCall, outputWrite);
        }

        private String mapTypeToPython(String type) {
            return switch (type) {
                case "int", "list<int>" -> "int";
                case "float", "list<float>" -> "float";
                case "string", "list<string>" -> "str";
                case "bool", "list<bool>" -> "bool"; // Note: Boolean parsing may require additional handling
                default -> "str"; // Default to string for unknown types
            };
        }

        public String generateJavaScript() {
            String inputReads = inputFields.stream()
                    .map(field -> {
                        if (field.getType().startsWith("list<")) {
                            return """
const size_%1$s = parseInt(input.shift());
const %1$s = input.splice(0, size_%1$s).map(%2$s);
""".formatted(field.getName(), mapTypeToJavaScript(field.getType()));
                        } else {
                            return "const %s = %s(input.shift());".formatted(field.getName(), mapTypeToJavaScript(field.getType()));
                        }
                    }).collect(Collectors.joining("\n  "));

            String functionCall = "const result = %s(%s);".formatted(
                    functionName,
                    inputFields.stream().map(Field::getName).collect(Collectors.joining(", "))
            );
            String outputWrite = "console.log(result);";

            return """
##USER_CODE_HERE##

const input = require('fs').readFileSync('/dev/stdin', 'utf8').trim().split('\\n').join(' ').split(' ');
%s
%s
%s
        """.formatted(inputReads, functionCall, outputWrite);
        }

        private String mapTypeToJavaScript(String type) {
            return switch (type) {
                case "int", "list<int>" -> "Number";
                case "float", "list<float>" -> "parseFloat";
                case "string", "list<string>" -> "String";
                case "bool", "list<bool>" -> "Boolean";
                default -> "String"; // Default for unknown types
            };
        }

        public String generateTypeScript() {
            String inputReads = inputFields.stream()
                    .map(field -> {
                        if (field.getType().startsWith("list<")) {
                            return """
const size_%1$s: number = parseInt(input.shift() as string);
const %1$s: %2$s[] = input.splice(0, size_%1$s).map(%3$s);
""".formatted(field.getName(), mapTypeToTypeScript(field.getType()), mapTypeToTypeScript(field.getType()).toLowerCase());
                        } else {
                            return "const %s: %s = %s(input.shift() as string);".formatted(
                                    field.getName(),
                                    mapTypeToTypeScript(field.getType()),
                                    mapTypeToTypeScript(field.getType()).toLowerCase()
                            );
                        }
                    }).collect(Collectors.joining("\n  "));

            String functionCall = "const result: %s = %s(%s);".formatted(
                    mapTypeToTypeScript(outputFields.get(0).getType()),
                    functionName,
                    inputFields.stream().map(Field::getName).collect(Collectors.joining(", "))
            );
            String outputWrite = "console.log(result);";

            return """
##USER_CODE_HERE##

const input: string[] = require('fs').readFileSync('/dev/stdin', 'utf8').trim().split('\\n').join(' ').split(' ');
%s
%s
%s
        """.formatted(inputReads, functionCall, outputWrite);
        }

        private String mapTypeToTypeScript(String type) {
            return switch (type) {
                case "int" -> "number";
                case "float" -> "number";
                case "string" -> "string";
                case "bool" -> "boolean";
                case "list<int>" -> "number[]";
                case "list<float>" -> "number[]";
                case "list<string>" -> "string[]";
                case "list<bool>" -> "boolean[]";
                default -> "any"; // Default for unknown types
            };
        }




        @AllArgsConstructor
        @NoArgsConstructor
        @Builder
        @Data
        public static class Field {
            private String type;
            private String name;
        }

    }
    String enrich(String code, int languageId){
        return code;
    }
}
