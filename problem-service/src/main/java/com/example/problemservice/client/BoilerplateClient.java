package com.example.problemservice.client;

import com.example.problemservice.exception.AppException;
import com.example.problemservice.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class BoilerplateClient {

    private static final Logger log = LoggerFactory.getLogger(BoilerplateClient.class);


    //    @Component
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Slf4j
    public static class BoilerPlateGenerator {
        private String problemName;
        private String functionName;
        private List<Field> inputFields;
        private List<Field> outputFields;

        public void parse(String structure) {
            String[] lines = structure.split("\n");
            String currentSection = null;
            inputFields = new ArrayList<>();
            outputFields = new ArrayList<>();

            for (String rawLine : lines) {
                String line = rawLine.trim();
                //System.out.println(line.startsWith("Input Field:") && "input".equals(currentSection));
                //System.out.println("- " + line);

                if (line.startsWith("Problem Name:")) {
                    this.problemName = extractQuoteValue(line);
                    //System.out.println("extract problem name: " + problemName);
                } else if (line.startsWith("Function Name:")) {
                    this.functionName = extractValue(line);
                    //System.out.println("extract function name: " + functionName);

                } else if (line.startsWith("Input Structure:")) {
                    currentSection = "input";
                    //System.out.println("input section");
                } else if (line.startsWith("Output Structure:")) {
                    currentSection = "output";
                    //System.out.println("output section");
                } else if (line.startsWith("Input Field:") && "input".equals(currentSection)) {
                    Field field = extractField(line);
                    if (field != null) {
                        inputFields.add(field);
                        //System.out.println("extract input: " + field.type + field.name);
                    }
                } else if (line.startsWith("Output Field:") && "output".equals(currentSection)) {
                    Field field = extractField(line);
                    if (field != null) {
                        outputFields.add(field);
                        //System.out.println("extract output: " + field.type + field.name);
                    }
                }
            }

            /*System.out.println("--------------------");

            for (Field field : inputFields) {
                System.out.println(field.getType() + " " + field.getName());
            }
            for (Field field : outputFields) {
                System.out.println(field.getType() + " " + field.getName());
            }
            System.out.println("--------------------");*/

        }

        private String extractQuoteValue(String line) {
            String regex = "Problem Name: \\\"(.*)\\\"$";
            return line.replaceAll(regex, "$1");
        }

        private String extractValue(String line) {
            String regex = "Function Name: (\\w+)$";
            return line.replaceAll(regex, "$1");
        }

        private Field extractField(String line) {
            String[] matches = line.split(" ");
            if (matches.length >= 4) {
                return new Field(matches[2], matches[3]);
            }
            return null;
        }

        //=================================== C++ ===========================================
        public String generateCpp() {
            String inputs = inputFields.stream()
                    .filter(field -> !field.getType().startsWith("list<"))
                    .map(field -> mapTypeToCpp(field.getType()) + " " + field.getName())
                    .collect(Collectors.joining("; \n"));

            inputs += ";\n";

            //log.info("inputs: {}", inputs);

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

            String outputWrite = null;

            if (outputFields.get(0).getType().startsWith("list<list<")) {
                outputWrite = """
                        bool isFirstSublist = true;
                            for (const auto &sublist : result) {
                                if (!isFirstSublist) std::cout << "\\n";
                                isFirstSublist = false;
                                                
                                std::cout << "[";
                                // Duyệt qua từng phần tử trong sublist
                                bool isFirstItem = true;
                                for (const auto &item : sublist) {
                                    if (!isFirstItem) std::cout << ", ";
                                    isFirstItem = false;
                                    std::cout << item;
                                }
                                std::cout << "]";
                            }""";
            } else if (outputFields.get(0).getType().startsWith("list<")) {
                outputWrite = "for (const auto &item : result) std::cout << item << ' ';\nstd::cout << std::endl;";
            } else {
                outputWrite = "std::cout << result << std::endl;";
            }

            return """
                    #include <iostream>
                    #include <vector>
                    #include <string>
                    using namespace std;
                                        

                    ##USER_CODE_HERE##

                    int main() {
                      %s
                      %s
                      %s
                      %s
                      return 0;
                    }
                            """.formatted(inputs, inputReads, functionCall, outputWrite);
        }

        public String generateFunctionCpp() {
            String inputs = String.join(", ", inputFields.stream()
                    .map(field -> mapTypeToCpp(field.getType()) + " " + field.getName())
                    .toArray(String[]::new));
            return String.format("""
                            #include <iostream>
                            #include <vector>
                            #include <string>
                            %s %s(%s) {\n    // Implementation goes here\n    return result;\n}
                            """,
                    mapTypeToCpp(outputFields.get(0).getType()), functionName, inputs);
        }

        private String mapTypeToCpp(String type) {
            if (type.startsWith("list<list<")) {
                String innerType = type.substring(10, type.length() - 2);
                return "std::vector<std::vector<" + mapTypeToCpp(innerType) + ">>";
            }

            if (type.startsWith("list<")) {
                String innerType = type.substring(5, type.length() - 1);
                return "std::vector<" + mapTypeToCpp(innerType) + ">";
            }

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

        //================================= Java =================================
        public String generateJava() {
            String inputReads = inputFields.stream()
                    .map(field -> {
                        if (field.getType().startsWith("list<")) {
                            return """
                                    int size_%1$s = scanner.nextInt();
                                    %2$s %1$s = new ArrayList<>();
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
            String outputWrite = "";
            if (outputFields.get(0).getType().startsWith("list<list<")) {
                outputWrite = """
                        for(List<?> sublist : result) {
                                        System.out.println(sublist);
                                    }
                        """;
            } else if (outputFields.get(0).getType().startsWith("list<")) {
                outputWrite = "System.out.println(result.stream().map(Object::toString).collect(Collectors.joining(\" \")));";
            } else {
                // For single value output
                outputWrite = "System.out.println(result);";
            }

            return """
                    import java.util.*;

                    public class Main {

                        ##USER_CODE_HERE##
                        
                        public static void main(String[] args) {
                            Scanner scanner = new Scanner(System.in);
                            %s
                            %s
                            %s
                        }
                    }
                            """.formatted(inputReads, functionCall, outputWrite);
        }

        public String generateFunctionJava() {
            StringBuilder javaCode = new StringBuilder();

            // Generate method signature
            String inputs = inputFields.stream()
                    .map(field -> mapTypeToJava(field.getType()) + " " + field.getName())
                    .collect(Collectors.joining(", "));
            String outputType = mapTypeToJava(outputFields.get(0).getType());
            javaCode.append(String.format("""
                    public static %s %s(%s) {\n
                    """, outputType, functionName, inputs));
            javaCode.append("    // Implementation goes here\n");
            javaCode.append("    return null;\n");
            javaCode.append("}\n");

            return javaCode.toString();
        }


        private String mapTypeToJava(String type) {
            // Handle list types separately
            if (type.startsWith("list<list<")) {
                String innerType = type.substring(10, type.length() - 2);
                return "List<List<" + mapTypeToJava(innerType) + ">>";
            } else if (type.startsWith("list<")) {
                String innerType = type.substring(5, type.length() - 1);
                return "List<" + mapTypeToJava(innerType) + ">";
            }

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

        //=================================== Python ========================================
        public String generatePython() {
            String inputReads = inputFields.stream()
                    .map(field -> {
                        if (field.getType().startsWith("list<")) {
                            return """
                                    size%1$s = int(input())
                                        %1$s = list(map(%2$s, input().split()[:size%1$s]))
                                    """.formatted(field.getName(), mapTypeToPython(field.getType()));
                        } else {
                            return "%s = %s(input())".formatted(field.getName(), mapTypeToPython(field.getType()));
                        }
                    }).collect(Collectors.joining("\n    "));

            String functionCall = "result = %s(%s)".formatted(
                    functionName,
                    inputFields.stream().map(Field::getName).collect(Collectors.joining(", "))
            );
            String outputWrite = null;
            if (outputFields.get(0).getType().startsWith("list<list<")) {
                outputWrite = "for sublist in result: print(sublist)\n";
            } else if (outputFields.get(0).getType().startsWith("list<")) {
                outputWrite = "print(' '.join(map(str, result)))";
            } else {
                outputWrite = "print(result)";
            }

            return """
                    import sys
                    ##USER_CODE_HERE##

                    if __name__ == '__main__':
                        %s
                        %s
                        %s
                            """.formatted(inputReads, functionCall, outputWrite);
        }

        public String generateFunctionPython() {
            StringBuilder pythonCode = new StringBuilder();

            // Generate method signature
            String inputs = inputFields.stream()
                    .map(Field::getName)
                    .collect(Collectors.joining(", "));
            pythonCode.append(String.format("""
                    import sys
                    def %s(%s):\n
                    """, functionName, inputs));
            pythonCode.append("    # Implementation goes here\n");
            pythonCode.append("    return None\n");

            return pythonCode.toString();
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

        //============================== Java Script ==============================================
        public String generateJavaScript() {
            String inputReads = inputFields.stream()
                    .map(field -> {
                        if (field.getType().startsWith("list<")) {
                            return """
                                    const size_%1$s = Number(input.shift());
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
            String outputWrite = "";// "console.log(result);";

            if (outputFields.get(0).getType().startsWith("list<list<")) {
                outputWrite = "result.forEach(sublist => {console.log(`[${sublist.join(\", \")}]`);});";
            } else if (outputFields.get(0).getType().startsWith("list<")) {
                outputWrite = "console.log(result.join(' '));";
            } else {
                outputWrite = "console.log(result);";
            }

            return """
                    ##USER_CODE_HERE##

                    const readline = require('readline');
                                        
                    const rl = readline.createInterface({
                         input: process.stdin,
                         output: process.stdout,
                    });
                                        
                    let input = [];
                                        
                    rl.on('line', (line) => {
                         input.push(...line.trim().split(/\\s+/));
                    }).on('close', () => {
                        %s
                        %s
                        %s
                    });
                            """.formatted(inputReads, functionCall, outputWrite);
        }

        public String generateFunctionJavaScript() {
            StringBuilder jsCode = new StringBuilder();

            // Generate function signature
            String inputs = inputFields.stream()
                    .map(Field::getName)
                    .collect(Collectors.joining(", "));
            jsCode.append(String.format("function %s(%s) {\n", functionName, inputs));
            jsCode.append("    // Implementation goes here\n");
            jsCode.append("    return null;\n");
            jsCode.append("}\n");

            return jsCode.toString();
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

        //================================ Type Script =================================
        public String generateTypeScript() {
            String inputReads = inputFields.stream()
                    .map(field -> {
                        if (field.getType().startsWith("list<")) {
                            String interType = field.getType().replace("list<", "").replace(">", "");
                            if (interType.equals("string")) {
                                return """
                                        const size_%1$s: number = parseInt(input.shift());
                                        const %1$s: string[] = input.splice(0, size_%1$s);"""
                                        .formatted(field.getName());

                            } else if (interType.equals("bool")) {
                                return """
                                        const size_%1$s: number = parseInt(input.shift());
                                        const %1$s: bool[] = input.splice(0, size_%1$s).map(str => str === 'true');
                                        """.formatted(field.getName());
                            }
                            return """
                                    const size_%1$s: number = parseInt(input.shift());
                                    const %1$s: number[] = input.splice(0, size_%1$s).map(Number);
                                    """.formatted(field.getName());
                        } else if (field.getType().startsWith("str")) {
                            return "const %s: string = input.shift();".formatted(field.getName());
                        } else if (field.getType().startsWith("bool")) {
                            return "const %s: boolean = input.shift() === 'true';".formatted(field.getName());
                        } else {
                            return "const %s: number = Number(input.shift());".formatted(
                                    field.getName()
                            );
                        }
                    }).collect(Collectors.joining("\n    "));

            String functionCall = "const result: %s = %s(%s);".formatted(
                    mapTypeToTypeScript(outputFields.get(0).getType()),
                    functionName,
                    inputFields.stream().map(Field::getName).collect(Collectors.joining(", "))
            );
            String outputWrite = "console.log(result);";
            if (outputFields.get(0).getType().startsWith("list<list<")) {
                outputWrite = "result.forEach((sublist: number[]) => {console.log(`[${sublist.join(\", \")}]`);});";
            } else if (outputFields.get(0).getType().startsWith("list<")) {
                outputWrite = "console.log(result.join(' '));";
            } else {
                outputWrite = "console.log(result);";
            }

            return """
                    ##USER_CODE_HERE##

                    const input: string[] = require('fs').readFileSync('/dev/stdin', 'utf8').trim().split('\\n').join(' ').split(' ');
                    %s
                    %s
                    %s
                            """.formatted(inputReads, functionCall, outputWrite);
        }

        public String generateFunctionTypeScript() {
            StringBuilder tsCode = new StringBuilder();

            // Generate function signature
            String inputs = inputFields.stream()
                    .map(field -> field.getName() + ": " + mapTypeToTypeScript(field.getType()))
                    .collect(Collectors.joining(", "));
            String outputType = mapTypeToTypeScript(outputFields.get(0).getType());
            tsCode.append(String.format("function %s(%s): %s {\n", functionName, inputs, outputType));
            tsCode.append("    // Implementation goes here\n");
            tsCode.append("    return null;\n");
            tsCode.append("}\n");

            return tsCode.toString();
        }

        private String mapTypeToTypeScript(String type) {
            if (type.startsWith("list<list")) {
                String innerType = type.substring(10, type.length() - 2);
                return mapTypeToTypeScript(innerType); // Assuming 2D list of the same type
            }
            return switch (type) {
                case "int", "float" -> "number";
                case "string" -> "string";
                case "bool" -> "boolean";
                case "list<int>", "list<float>" -> "number[]";
                case "list<string>" -> "string[]";
                case "list<bool>" -> "boolean[]";
                default -> "any"; // Default for unknown types
            };
        }


        //================================ C =====================================
        public String generateC() {
            StringBuilder inputReads = new StringBuilder();
            for (Field field : inputFields) {
                if (field.getType().startsWith("list<")) {
                    inputReads.append("""
                            int size_%1$s;
                            scanf("%%d", &size_%1$s);
                            %2$s %1$s[size_%1$s];
                            for (int i = 0; i < size_%1$s; ++i) {
                                scanf("%%d", &%1$s[i]);
                            }
                            """.formatted(field.getName(), mapTypeToC(field.getType().replace("list<", "").replace(">", ""))));
                } else {
                    inputReads.append("""
                            %s %s;
                            scanf("%%%s", &%s);
                            """.formatted(mapTypeToC(field.getType()), field.getName(), mapSpecifierToC(field.getType()), field.getName()));
                }
            }

            for (Field field : inputFields) {
                if (field.getType().startsWith("list<")) {
                    inputFields.add(new Field("size_" + field.getName(), "int"));
                }
            }

            String functionCall = "\nint size_result = 0;\n%s result = %s(%s);".formatted(
                    mapTypeToC(outputFields.get(0).getType()),
                    functionName,
                    inputFields.stream().map(Field::getName).collect(Collectors.joining(", ")) + ", size_result"
            );

            String outputWrite = """
                    printf("%%d\\n", result);
                    """;
            if (outputFields.get(0).getType().startsWith("list<list<")) {
                outputWrite = "printf(\"Currently Not Support 2D array for C.\\n\");";
            } else if (outputFields.get(0).getType().startsWith("list<")) {
                outputWrite = "for (int i = 0; i < size_result; ++i) printf(\"%d \", result[i]);\nprintf(\"\\n\");";
            }

            return """
                    #include <stdio.h>

                    ##USER_CODE_HERE##

                    int main() {
                    %s
                    %s
                    %s
                        return 0;
                    }
                    """.formatted(inputReads, functionCall, outputWrite);
        }

        public String generateFunctionC() {
            StringBuilder cCode = new StringBuilder();

            // Generate function signature
            String inputs = inputFields.stream()
                    .map(field -> mapTypeToC(field.getType()) + " " + field.getName())
                    .collect(Collectors.joining(", "));
            for (Field field : inputFields) {
                if (field.getType().startsWith("list<")) {
                    inputs += String.format(", int size_%s", field.getName());
                }
            }

            inputs += ", int size_result"; // For output size if needed

            String outputType = mapTypeToC(outputFields.get(0).getType());
            cCode.append(String.format("%s %s(%s) {\n", outputType, functionName, inputs));
            cCode.append("    // Implementation goes here\n");
            cCode.append("    return result;\n");
            cCode.append("}\n");

            return cCode.toString();
        }

        private String mapTypeToC(String type) {
            return switch (type) {
                case "int" -> "int";
                case "float" -> "float";
                case "string" -> "char*";
                case "bool" -> "int"; // In C, boolean values are usually represented as integers
                case "list<int>" -> "int*";
                case "list<float>" -> "float*";
                default -> "void*"; // For unknown types
            };
        }

        private String mapSpecifierToC(String type) {
            return switch (type) {
                case "int" -> "d";
                case "float" -> "f";
                case "string" -> "s";
                case "bool" -> "d";
                default -> "";
            };
        }

        //=========================================== C# ====================================================
        public String generateCSharp() {
            String inputReads = inputFields.stream()
                    .map(field -> {
                        if (field.getType().startsWith("list<")) {
                            return """
                                    int size_%1$s = int.Parse(Console.ReadLine());
                                    var %1$s = new List<%2$s>();
                                    for (int i = 0; i < size_%1$s; i++) {
                                        %1$s.Add(%2$s.Parse(Console.ReadLine()));
                                    }
                                    """.formatted(field.getName(), mapTypeToCSharp(field.getType().replace("list<", "").replace(">", "")));
                        } else {
                            return "%s %s = %s.Parse(Console.ReadLine());".formatted(
                                    mapTypeToCSharp(field.getType()),
                                    field.getName(),
                                    mapTypeToCSharp(field.getType())
                            );
                        }
                    }).collect(Collectors.joining("\n  "));

            String functionCall = "%s result = %s(%s);".formatted(
                    mapTypeToCSharp(outputFields.get(0).getType()),
                    functionName,
                    inputFields.stream().map(Field::getName).collect(Collectors.joining(", "))
            );

            String outputWrite = "Console.WriteLine(result);";

            if (outputFields.get(0).getType().startsWith("list<list<")) {
                outputWrite = "foreach (var list in result){Console.WriteLine($\"[{string.Join(\", \", list)}]\");}";
            } else if (outputFields.get(0).getType().startsWith("list<")) {
                outputWrite = "Console.WriteLine(string.Join(\" \", result));";
            }

            return """
                    using System;
                    using System.Collections.Generic;

                    ##USER_CODE_HERE##

                    class Program {
                        static void Main() {
                    %s
                    %s
                    %s
                        }
                    }
                    """.formatted(inputReads, functionCall, outputWrite);
        }

        public String generateFunctionCSharp() {
            StringBuilder cSharpCode = new StringBuilder();

            // Generate function signature
            String inputs = inputFields.stream()
                    .map(field -> mapTypeToCSharp(field.getType()) + " " + field.getName())
                    .collect(Collectors.joining(", "));
            String outputType = mapTypeToCSharp(outputFields.get(0).getType());
            cSharpCode.append(String.format("public %s %s(%s) {\n", outputType, functionName, inputs));
            cSharpCode.append("    // Implementation goes here\n");
            cSharpCode.append("    return default;\n");
            cSharpCode.append("}\n");

            return cSharpCode.toString();
        }

        private String mapTypeToCSharp(String type) {
            if (type.startsWith("list<list<")) {
                String innerType = type.substring(10, type.length() - 2);
                return "List<List<" + mapTypeToCSharp(innerType) + ">>";
            }
            return switch (type) {
                case "int" -> "int";
                case "float" -> "float";
                case "string" -> "string";
                case "bool" -> "bool";
                case "list<int>" -> "List<int>";
                case "list<float>" -> "List<float>";
                case "list<string>" -> "List<string>";
                case "list<bool>" -> "List<bool>";
                default -> "object"; // Default for unknown types
            };
        }

        public static String defaultCodeGenerator(String structure, int languageId) {
            BoilerPlateGenerator parser = new BoilerPlateGenerator();
            parser.parse(structure);
//            System.out.println(structure);
//            System.out.println(parser.functionName);
            return switch (languageId) {
                case 48, 49, 50 -> parser.generateFunctionC();
                case 51 -> parser.generateFunctionCSharp();
                case 52, 53, 54 -> parser.generateFunctionCpp();
                case 62, 91 -> parser.generateFunctionJava();
                case 63 -> parser.generateFunctionJavaScript();
                case 71 -> parser.generateFunctionPython();
                case 74 -> parser.generateFunctionTypeScript();
                default -> throw new AppException(ErrorCode.INVALID_PROGRAMMING_LANGUAGE);
            };
            //return defaultCode;
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


    //
    public String enrich(String code, int languageId, String structure) {
        BoilerPlateGenerator parser = new BoilerPlateGenerator();
        parser.parse(structure);

        String enrichBoilerPlate = switch (languageId) {
            case 48, 49, 50 -> parser.generateC();
            case 51 -> parser.generateCSharp();
            case 52, 53, 54 -> parser.generateCpp();
            case 62, 91 -> parser.generateJava();
            case 63 -> parser.generateJavaScript();
            case 71 -> parser.generatePython();
            case 74 -> parser.generateTypeScript();
            default -> throw new AppException(ErrorCode.INVALID_PROGRAMMING_LANGUAGE);
        };

        return enrichBoilerPlate.replace("##USER_CODE_HERE##", code);

    }

    public String normalizeLanguage(String raw) {
        if (raw == null || raw.isEmpty()) return "";

        int spaceIdx = raw.indexOf(' ');

        String cleaned = (spaceIdx >= 0) ? raw.substring(0, spaceIdx) : raw;

        cleaned = cleaned.toLowerCase();

        return switch (cleaned) {
            case "c++", "cpp" -> "cpp";
            case "c#", "csharp" -> "csharp";
            case "javascript", "js" -> "javascript";
            case "typescript" -> "typescript";
            case "python" -> "python";
            case "java" -> "java";
            case "c" -> "c";
            default -> cleaned;
        };
    }

    public String extractFunctionCode(String code, String language, String structure) {
        BoilerPlateGenerator parser = new BoilerPlateGenerator();
        parser.parse(structure);

        String regex = null;

        String functionName = parser.functionName;
        String normalLanguage = normalizeLanguage(language);
        regex = switch (normalLanguage.toLowerCase()) {
            case "cpp", "c++" ->
                // C++ function regex
                    String.format(
                            "(?s)([ \\t]*(?:[\\w:<>,\\s*&]+)[ \\t]+%s\\s*\\([^\\)]*\\)\\s*\\{.*?\\})",
                            Pattern.quote(functionName)
                    );
            case "java" -> String.format(
                    "(?s)(public\\s+static\\s+[\\w<>\\[\\]]+\\s+%s\\s*\\([^\\)]*\\)\\s*\\{.*?\\})",
                    Pattern.quote(functionName)
            );
            case "python" -> String.format(
                    "(?m)(def\\s+%s\\s*\\([^\\)]*\\):\\n(?:[ \\t]+.*\\n)+)",
                    Pattern.quote(functionName)
            );
            case "javascript", "js" -> String.format(
                    "(?s)(function\\s+%s\\s*\\([^\\)]*\\)\\s*\\{.*?\\})",
                    Pattern.quote(functionName)
            );
            case "typescript", "ts" -> String.format(
                    "(?s)(function\\s+%s\\s*\\([^\\)]*\\)\\s*:\\s*[^\\s\\{]+\\s*\\{.*?\\})",
                    Pattern.quote(functionName)
            );
            case "c" -> String.format(
                    "(?s)([\\w\\*\\s]+\\s+%s\\s*\\([^\\)]*\\)\\s*\\{.*?\\})",
                    Pattern.quote(functionName)
            );
            case "c#", "csharp" -> String.format(
                    "(?s)(public\\s+[\\w<>\\[\\]]+\\s+%s\\s*\\([^\\)]*\\)\\s*\\{.*?\\})",
                    Pattern.quote(functionName)
            );
            default -> throw new IllegalArgumentException("Unsupported language: " + normalLanguage);
        };

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(code);

        if (matcher.find()) {
            return matcher.group(1).trim();
        } else {
            return "// Function not found.";
        }
    }

}
    /*String enrich(String code, int languageId){
        return code;
    }*/

/*String functionCall = "const result = %s(%s);".formatted(
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
        */