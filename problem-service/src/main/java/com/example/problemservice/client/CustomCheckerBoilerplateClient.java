package com.example.problemservice.client;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CustomCheckerBoilerplateClient {

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Slf4j
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class CustomCheckerBoilerPlateGenerator {
        @AllArgsConstructor
        @NoArgsConstructor
        @Builder
        @Data
        public static class Field {
            private String type;
            private String name;
        }

        String problemName;
        String functionName;
        List<Field> inputFields;
        List<Field> outputFields;


        public String generateCppFullBoilerplate() {
            String inputs = inputFields.stream()
                    .filter(field -> !field.getType().startsWith("list<"))
                    .map(field -> mapTypeToCpp(field.getType()) + " " + field.getName())
                    .collect(Collectors.joining("; \n"));

            inputs += ";\n";

            //log.info("inputs: {}", inputs);

            /*
            * "\n  for(int i = 0; i < size_" + field.getName() + "; ++i) {\n    int size_sublist;\n    std::cin >> size_sublist;\n    " +
                                    field.getName() + "[i].resize(size_sublist);\n    for(int j = 0; j < size_sublist; ++j) std::cin >> " + field.getName() + "[i][j];\n  }";
            * */

            /*if (field.getType().startsWith("list<string")) {
                            return "int size_" + field.getName() + ";\n  std::cin >> size_" + field.getName() + ";\n  " +
                                    mapTypeToCpp(field.getType()) + " "
                                    + field.getName() + "(size_" + field.getName() + ");" +
                                    "\n  for(int i = 0; i < size_" + field.getName() + "; ++i) {\n    std::getline(std::cin, line);\n    std::istringstream sublistStream(line);\n    int size_sublist;\n    sublistStream >> size_sublist;\n    " +
                                    field.getName() + "[i].resize(size_sublist);\n    for(int j = 0; j < size_sublist; ++j) sublistStream >> " + field.getName() + "[i][j];\n  }";
                        } else */

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

            String outputWrite =  null;

            if (outputFields.get(0).getType().startsWith("list<list<")) {
                outputWrite = """
                        bool isFirstSublist = true;
                            for (const auto &sublist : result) {
                                if (!isFirstSublist) std::cout << "\\n";
                                isFirstSublist = false;
                        
                                std::cout << "[";
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
                outputWrite = """
                        if (result == true) {
                            std::cout << "true" << std::endl;
                        }
                        else {
                            std::cout << "false" << std::endl;
                        }
                        """;
            }

            return """
                    #include <iostream>
                    #include <vector>
                    #include <string>
                    #include <sstream>
                    #include <unordered_map>
                    #include <fstream>
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
    }

    //enrich: input field is the last output submission field
    public String enrichCode(BoilerplateClient.BoilerPlateGenerator boilerPlateGenerator, String code) {
        CustomCheckerBoilerPlateGenerator customCheckerBoilerPlateGenerator = new CustomCheckerBoilerPlateGenerator();

        customCheckerBoilerPlateGenerator.setProblemName("customChecker");
        customCheckerBoilerPlateGenerator.setFunctionName("customChecker");

        List<CustomCheckerBoilerPlateGenerator.Field> outputFields1 = boilerPlateGenerator.getOutputFields()
                .stream()
                        .map(field -> {
                            CustomCheckerBoilerPlateGenerator.Field inputField = new CustomCheckerBoilerPlateGenerator.Field();
                            inputField.setType(field.getType());
                            inputField.setName("actual_" + field.getName());
                            return inputField;
                        }).toList();

        List<CustomCheckerBoilerPlateGenerator.Field> outputFields2 = boilerPlateGenerator.getOutputFields()
                .stream()
                        .map(field -> {
                            CustomCheckerBoilerPlateGenerator.Field inputField = new CustomCheckerBoilerPlateGenerator.Field();
                            inputField.setType(field.getType());
                            inputField.setName("expected_" + field.getName());
                            return inputField;
                        }).toList();

        ArrayList<CustomCheckerBoilerPlateGenerator.Field> outputFields = new ArrayList<>();

        outputFields.addAll(outputFields1);
        outputFields.addAll(outputFields2);

                /*.forEach(field -> {
                    CustomCheckerBoilerPlateGenerator.Field inputField = new CustomCheckerBoilerPlateGenerator.Field();
                    inputField.setType(field.getType());
                    inputField.setName(field.getName());
                    customCheckerBoilerPlateGenerator.getInputFields().add(inputField);
                });*/

        customCheckerBoilerPlateGenerator.setInputFields(outputFields);

        CustomCheckerBoilerPlateGenerator.Field outputField =  CustomCheckerBoilerPlateGenerator.Field.builder()
                .name("result")
                .type("bool")
                .build();

        customCheckerBoilerPlateGenerator.setOutputFields(List.of(outputField));

        String enrichBoilerplate = customCheckerBoilerPlateGenerator.generateCppFullBoilerplate();

        return enrichBoilerplate.replace("##USER_CODE_HERE##", code);
    }
}
