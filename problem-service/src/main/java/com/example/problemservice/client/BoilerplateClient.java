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
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class BoilerplateClient {

    private static final Logger log = LoggerFactory.getLogger(BoilerplateClient.class);


    //    @Component
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

        public static final Map<Integer, String> problemCategoryMap = Map.of(
                11, "Tree",
                12, "Graph",
                9, "Dynamic Programming",
                15, "Backtracking"
        );

        final String EXPECTED_OUTPUT_FIELD_NAME = "expectedOutput";
        final String treeNodeDefinitionCpp = """
                class TreeNode {
                    public:
                        int val;
                        TreeNode* left;
                        TreeNode* right;
                
                        TreeNode(int x) { val = x; left = nullptr; right = nullptr; }
                        TreeNode(int x, TreeNode* left, TreeNode* right) : val(x), left(left), right(right) {}
                        TreeNode* buildTree(const vector<string>& nodes);
                        TreeNode* buildTree(const vector<int>& nodes) ;
                };
                """;

        final String treeBuilderDefinitionCpp = """
                    TreeNode* buildTree(const vector<string>& nodes) {
                            if (nodes.empty() || nodes[0] == "null") return nullptr;
                
                            TreeNode* root = new TreeNode(stoi(nodes[0]));
                            queue<TreeNode*> q;
                            q.push(root);
                            int i = 1;
                
                            while (!q.empty() && i < nodes.size()) {
                                TreeNode* current = q.front();
                                q.pop();
                
                                // Left child
                                if (i < nodes.size() && nodes[i] != "null") {
                                    current->left = new TreeNode(stoi(nodes[i]));
                                    q.push(current->left);
                                }
                                i++;
                
                                // Right child
                                if (i < nodes.size() && nodes[i] != "null") {
                                    current->right = new TreeNode(stoi(nodes[i]));
                                    q.push(current->right);
                                }
                                i++;
                            }
                    
                            return root;
                        }
                        
                    TreeNode* buildTree(const vector<int>& nodes) {
                        if (nodes.empty() || nodes[0] == -1) return nullptr;
                    
                        TreeNode* root = new TreeNode(nodes[0]);
                        queue<TreeNode*> q;
                        q.push(root);
                        int i = 1;
                    
                        while (!q.empty() && i < nodes.size()) {
                            TreeNode* current = q.front();
                            q.pop();
                    
                            // Left child
                            if (i < nodes.size() && nodes[i] != -1) {
                                current->left = new TreeNode(nodes[i]);
                                q.push(current->left);
                            }
                            i++;
                    
                            // Right child
                            if (i < nodes.size() && nodes[i] != -1) {
                                current->right = new TreeNode(nodes[i]);
                                q.push(current->right);
                            }
                            i++;
                        }
                    
                        return root;
                    }
                """;

        final String treeNodeDefinitionPython = """
                class TreeNode:
                    def __init__(self, x):
                        self.val = x
                        self.left = None
                        self.right = None
                """;

        final String treeBuilderDefinitionPython = """
               def build_tree(nodes):
                    if not nodes or nodes[0] == "null":
                        return None

                    root = TreeNode(int(nodes[0]))
                    queue = [root]
                    i = 1

                    while queue and i < len(nodes):
                        current = queue.pop(0)

                        # Left child
                        if i < len(nodes) and nodes[i] != "null":
                            current.left = TreeNode(int(nodes[i]))
                            queue.append(current.left)
                        i += 1

                        # Right child
                        if i < len(nodes) and nodes[i] != "null":
                            current.right = TreeNode(int(nodes[i]))
                            queue.append(current.right)
                        i += 1

                    return root
               """;

        final String treeNodeDefinitionJavascript = """
                class TreeNode {
                    constructor(x) {
                        this.val = x;
                        this.left = null;
                        this.right = null;
                    }
                }
                """;

        final String treeBuilderDefinitionJavascript = """
                function buildTree(nodes) {
                    if (!nodes || nodes.length === 0 || nodes[0] === "null") return null;

                    const root = new TreeNode(parseInt(nodes[0]));
                    const queue = [root];
                    let i = 1;

                    while (queue.length > 0 && i < nodes.length) {
                        const current = queue.shift();

                        // Left child
                        if (i < nodes.length && nodes[i] !== "null") {
                            current.left = new TreeNode(parseInt(nodes[i]));
                            queue.push(current.left);
                        }
                        i++;

                        // Right child
                        if (i < nodes.length && nodes[i] !== "null") {
                            current.right = new TreeNode(parseInt(nodes[i]));
                            queue.push(current.right);
                        }
                        i++;
                    }

                    return root;
                }
                """;

        final String treeNodeDefinitionJava = """
                public class TreeNode {
                    public int val;
                    public TreeNode left;
                    public TreeNode right;

                    public TreeNode(int x) {
                        this.val = x;
                        this.left = null;
                        this.right = null;
                    }
                }
                """;
        final String treeBuilderDefinitionJava = """
                public class TreeBuilder
                {
                    public static Main.TreeNode buildTree(List<String> nodes) {
                        if (nodes == null || nodes.isEmpty() || "null".equals(nodes.get(0))) return null;

                        Main.TreeNode root = new Main.TreeNode(Integer.parseInt(nodes.get(0)));
                        Queue<Main.TreeNode> queue = new LinkedList<>();
                        queue.add(root);
                        int i = 1;

                        while (!queue.isEmpty() && i < nodes.size()) {
                            Main.TreeNode current = queue.poll();

                            // Left child
                            if (i < nodes.size() && !"null".equals(nodes.get(i))) {
                                current.left = new Main.TreeNode(Integer.parseInt(nodes.get(i)));
                                queue.add(current.left);
                            }
                            i++;

                            // Right child
                            if (i < nodes.size() && !"null".equals(nodes.get(i))) {
                                current.right = new Main.TreeNode(Integer.parseInt(nodes.get(i)));
                                queue.add(current.right);
                            }
                            i++;
                        }

                        return root;
                    }
                }
                """;
        /* """
                class TreeBuilder {
                public:
                    static TreeNode* buildTree(const std::vector<int>& values) {
                        if (values.empty()) return nullptr;
                        std::queue<TreeNode*> queue;
                        TreeNode* root = new TreeNode(values[0]);
                        queue.push(root);
                        for (size_t i = 1; i < values.size(); ++i) {
                            TreeNode* current = queue.front();
                            queue.pop();
                            if (values[i] != -1) {
                                current->left = new TreeNode(values[i]);
                                queue.push(current->left);
                            }
                            if (++i < values.size() && values[i] != -1) {
                                current->right = new TreeNode(values[i]);
                                queue.push(current->right);
                            }
                        }
                        return root;
                    }
                };
                """;*/

        public void parse(String structure, Boolean hasCustomChecker) {
            if (hasCustomChecker == null) {
                hasCustomChecker = false;
            }

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

            if (hasCustomChecker) {
                Field expectedOutputField = new Field(outputFields.get(0).getType(), EXPECTED_OUTPUT_FIELD_NAME);
                inputFields.add(expectedOutputField);
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
        public String generateCpp(Boolean hasCustomChecker, String additionalCheckerFields, Hashtable<Integer, Boolean> categoryIds) {
            if (hasCustomChecker == null) {
                hasCustomChecker = false;
            }

            String inputs = inputFields.stream()
                    .filter(field -> !field.getType().startsWith("list<") && !field.getType().startsWith("tree<") && !field.getType().startsWith("graph<")) //todo: handle checker field
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

                        /*"int rowSize_" + field.getName() + ", colSize_ " + field.getName() + ";\n  std::cin >> rowSize_" + field.getName() + ";\n  "
                                    + "std::cin >> colSize_" + field.getName() + ";\n " +
                                    mapTypeToCpp(field.getType()) + " "
                                    + field.getName() + "(size_" + field.getName() + ");" +
                                    "\n  for(int i = 0; i < size_" + field.getName() + "; ++i) {\n    std::getline(std::cin, line);\n    std::istringstream sublistStream(line);\n    int size_sublist;\n    sublistStream >> size_sublist;\n    " +
                                    field.getName() + "[i].resize(size_sublist);\n    for(int j = 0; j < size_sublist; ++j) sublistStream >> " + field.getName() + "[i][j];\n  }";
                                     */
            /*
            * int rowSize_%1$s, colSize_%1$s;
                                        std::cin >> rowSize_%1$s >> colSize_%1$s;
                                        %2$s %1$s(rowSize_%1$s, std::vector<%3$s>(colSize_%1$s));
                                        for (int i = 0; i < rowSize_%1$s; ++i) {
                                            for (int j = 0; j < colSize_%1$s; ++j) {
                                                std::cin >> %1$s[i][j];
                                            }
                                            * std::cin.ignore(std::numeric_limits<std::streamsize>::max(), '\n'); // Ignore the newline character after reading rowSize
                                        }*/
            //for(int i = 0; i < size_strs; ++i) std::cout << strs[i]  << " ";

            /*
            * cout<< endl << "Input size: " << strs.size() << endl;
              for(int i = 0; i < strs.size(); ++i) std::cout <<"size: " << strs[i].length() << " -" << strs[i]  << " ";
            * */

            /*
            *
                                        std::cout << endl <<  "Result size: " << expectedOutput.size() << std::endl;
                                                                         for (int i = 0; i < expectedOutput.size(); ++i) {
                                                                                 for (int j = 0; j < expectedOutput[i].size(); ++j) {
                                                                                     std::cout<< "size: " << expectedOutput[i][j].length() << " -"<<expectedOutput[i][j] << " ";
                                                                                 }
                                                                                 std::cout<< std::endl;
                                                                         }
            * */
            String inputReads = inputFields.stream()
                    .map(field -> {
                        //2d array string
                        if (field.getType().startsWith("list<list<string")) {
                            return """
                                        int rowSize_%1$s;
                                        std::cin >> rowSize_%1$s;
                                        //std::cin.ignore();
                                        std::cin.ignore(std::numeric_limits<std::streamsize>::max(), '\\n');
                                        %2$s %1$s(rowSize_%1$s);
                                        for (int i = 0; i < rowSize_%1$s; ++i) {
                                                std::string line;
                                                std::getline(std::cin, line); // Read the entire line
                                                std::istringstream iss(line); // Create a string stream to split the line
                                                std::string word;
                                                while (iss >> word) { // Split the line into words
                                                    %1$s[i].push_back(word);
                                                }
                                        }
                                    """.formatted(field.getName(), mapTypeToCpp(field.getType()), mapTypeToCpp(field.getType().substring(10, field.getType().length() - 2)));
                        } else if (field.getType().startsWith("list<list<int")) {
                            return """
                                        int rowSize_%1$s;
                                        std::cin >> rowSize_%1$s;
                                        //std::cin.ignore();
                                        std::cin.ignore(std::numeric_limits<std::streamsize>::max(), '\\n');
                                        %2$s %1$s(rowSize_%1$s);
                                        for (int i = 0; i < rowSize_%1$s; ++i) {
                                                std::string line;
                                                std::getline(std::cin, line); // Read the entire line
                                                std::istringstream iss(line); // Create a string stream to split the line
                                                int value;
                                                while (iss >> value) { // Split the line into words
                                                    %1$s[i].push_back(value);
                                                }
                                        }
                                    """.formatted(field.getName(), mapTypeToCpp(field.getType()), mapTypeToCpp(field.getType().substring(10, field.getType().length() - 2)));
                        } else if (field.getType().startsWith("list<list<")) {
                            return """
                                        int rowSize_%1$s, colSize_%1$s;
                                        std::cin >> rowSize_%1$s >> colSize_%1$s;
                                        %2$s %1$s(rowSize_%1$s, std::vector<%3$s>(colSize_%1$s));
                                        for (int i = 0; i < rowSize_%1$s; ++i) {
                                            for (int j = 0; j < colSize_%1$s; ++j) {
                                                std::cin >> %1$s[i][j];
                                            }
                                            std::cin.ignore(std::numeric_limits<std::streamsize>::max(), '\\n'); // Ignore the newline character after reading rowSize
                                        }
                                    """.formatted(field.getName(), mapTypeToCpp(field.getType()), mapTypeToCpp(field.getType().substring(10, field.getType().length() - 2)));
                        } else if (field.getType().startsWith("list<int>")) {
                            return "int size_" + field.getName() + ";\n  std::cin >> size_" + field.getName() + ";\n  " +
                                    mapTypeToCpp(field.getType()) + " "
                                    + field.getName() + "(size_" + field.getName() + ");" +
                                    "\n  for(int i = 0; i < size_" + field.getName() + "; ++i) std::cin >> " + field.getName() + "[i];";
                        } else if (field.getType().startsWith("list<float>")) {
                            return "int size_" + field.getName() + ";\n  std::cin >> size_" + field.getName() + ";\n  " +
                                    mapTypeToCpp(field.getType()) + " "
                                    + field.getName() + "(size_" + field.getName() + ");" +
                                    "\n  for(int i = 0; i < size_" + field.getName() + "; ++i) std::cin >> " + field.getName() + "[i];";
                        } else if (field.getType().startsWith("list<string")) {
                            return "int size_" + field.getName() + ";\n  std::cin >> size_" + field.getName() + ";\n  " +
                                    mapTypeToCpp(field.getType()) + " "
                                    + field.getName() + "(size_" + field.getName() + ");" +
                                    "\n  for(int i = 0; i < size_" + field.getName() + "; ++i) {" +
                                    "\nstd::cin >> " + field.getName() + "[i];\n" +
                                    field.getName() + "[i].erase(std::remove(" + field.getName() + "[i].begin(), " + field.getName() + "[i].end(), '\\n'), " + field.getName() + "[i].end());\n" +
                                    field.getName() + "[i].erase(std::remove(" + field.getName() + "[i].begin(), " + field.getName() + "[i].end(), '\\r'), " + field.getName() + "[i].end());" +
                                    "}";
                        } else if (field.getType().startsWith("graph<int")) {
                            // read from adjacency list
                            return """
                                    int size_%1$s;
                                    std::cin >> size_%1$s;
                                    std::cin.ignore(std::numeric_limits<std::streamsize>::max(), '\\n');
                                    %2$s %1$s(size_%1$s);
                                    for (int i = 0; i < size_%1$s; ++i) {
                                        std::string line;
                                        std::getline(std::cin, line); // Read the entire line
                                    
                                        if (line.empty()) {
                                            continue; // Move to the next node
                                        }
                                        std::istringstream iss(line); // Create a string stream to split the line
                                        int neighbor;
                                        while (iss >> neighbor) { // Split the line into neighbors
                                            %1$s[i].push_back(neighbor);
                                        }
                                    }
                                    """.formatted(field.getName(), mapTypeToCpp(field.getType()));

                            /*
                            * std::cout << endl <<  "graph size: " << %1$s.size() << std::endl;
                                                                     for (int i = 0; i < %1$s.size(); ++i) {
                                                                             for (int j = 0; j < %1$s[i].size(); ++j) {
                                                                                 std::cout << %1$s[i][j] << " ";
                                                                             }
                                                                             std::cout<< std::endl;
                                                                     }
                            * */

                        } else if (field.getType().startsWith("list<")) {
                            return "int size_" + field.getName() + ";\n  std::cin >> size_" + field.getName() + ";\n  " +
                                    mapTypeToCpp(field.getType()) + " "
                                    + field.getName() + "(size_" + field.getName() + ");" +
                                    "\n  for(int i = 0; i < size_" + field.getName() + "; ++i) std::cin >> " + field.getName() + "[i];";
                        } else if (field.getType().startsWith("tree<")) {
                            return """
                                    std::string treeInput;
                                    std::getline(std::cin, treeInput);
                                    std::vector<std::string> nodes;
                                    std::istringstream iss(treeInput);
                                    std::string node;
                                    while (iss >> node) {
                                        nodes.push_back(node);
                                    }
                                    TreeNode* %s = buildTree(nodes);
                                    """.formatted(field.getName());
                        } else if (field.getType().startsWith("str")) {
                            return "std::string " + field.getName() + ";\n  std::getline(std::cin, " + field.getName() + ");";
                        } else if (field.getType().startsWith("bool")) {
                            return "bool " + field.getName() + ";\n  std::cin >> " + field.getName() + ";";
                        } else {
                            return "std::cin >> " + field.getName() + ";";
                        }
                    }).collect(Collectors.joining("\n "));
            String outputType = mapTypeToCpp(outputFields.get(0).getType());

            String functionCall = outputType + " result = " + functionName + "(" +
                    inputFields.stream()
                            .filter(field -> !field.getName().contains(EXPECTED_OUTPUT_FIELD_NAME)) //todo: handle checker field
                            .map(Field::getName)
                            .collect(Collectors.joining(", ")) + ");";

            String outputWrite = null;

            /*
            * std::cout << endl <<  "Result size: " << result.size() << std::endl;
                                 for (int i = 0; i < result.size(); ++i) {
                                         for (int j = 0; j < result[i].size(); ++j) {
                                             std::cout<< "size: " << result[i][j].length() << " -"<<result[i][j] << " ";
                                         }
                                         std::cout<< std::endl;
                                 }

            * */
            if (outputFields.get(0).getType().startsWith("list<list<bool>>")){
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
                                    std::cout << (item ? "true" : "false");
                                }
                                std::cout << "]";
                            }""";
            } else if (outputFields.get(0).getType().startsWith("list<list<")) {
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
            } else if (outputFields.get(0).getType().startsWith("list<bool>")){
                outputWrite = "for (const auto &item : result) std::cout << (item ? \"true\" : \"false\") << ' ';\nstd::cout << std::endl;";
            } else if (outputFields.get(0).getType().startsWith("list<")) {
                outputWrite = "for (const auto &item : result) std::cout << item << ' ';\nstd::cout << std::endl;";
            } else if (outputFields.get(0).getType().startsWith("tree<")) {
                //todo: tien implement tree output
            } else if (outputFields.get(0).getType().startsWith("bool")){
                outputWrite = "std::cout << (result ? \"true\" : \"false\") << std::endl;";
            } else {
                outputWrite = "std::cout << result << std::endl;";
            }


            /*if (hasCustomChecker && outputFields.get(0).getType().startsWith("list<")) {
                outputWrite = "std::cout << result.size() << std::endl;\n" + outputWrite;
            }*/

            if (hasCustomChecker) { // && outputFields.get(0).getType().startsWith("list<")
                String functionCustomCheckerCall = "bool isPassed = customChecker(result,expectedOutput";

                if (additionalCheckerFields != null && !additionalCheckerFields.isEmpty()) {
                    String[] additionalFields = additionalCheckerFields.split(",");
                    String functionCallWithAdditionalFields = String.join(", ", additionalFields);

                    if (!functionCallWithAdditionalFields.isEmpty()) {
                        functionCustomCheckerCall += ", " + functionCallWithAdditionalFields;
                    }
                }

                outputWrite = functionCustomCheckerCall + ");\n" + outputWrite + """
                        if (isPassed) {
                            std::cout << std::endl << "true" << std::endl;
                        } else {
                            std::cout << std::endl << "false" << std::endl;
                        }
                        """;
            }

            StringBuilder additionalDataStructureDeclaration = new StringBuilder();

            if (categoryIds != null && categoryIds.containsKey(11) && categoryIds.get(11)) {
                additionalDataStructureDeclaration
                        .append(treeNodeDefinitionCpp)
                        .append("\n")
                        .append(treeBuilderDefinitionCpp);
            }

            return """
                    #include <iostream>
                    #include <vector>
                    #include <string>
                    #include <algorithm>
                    
                    
                    #include <sstream>
                    #include <unordered_map>
                    #include <fstream>
                    #include <queue>
                    #include <limits>
                    #include <algorithm>
                    #include <set>
                    using namespace std;
                                        
                    
                    %s

                    ##USER_CODE_HERE##

                    int main() {
                      %s
                      %s
                      %s
                      %s
                      return 0;
                    }
                            """.formatted(additionalDataStructureDeclaration, inputs, inputReads, functionCall, outputWrite);
        }

        /*
        *
                      std::cout << endl <<  "expected output size: " << expectedOutput.size() << std::endl;
                       for (int i = 0; i < expectedOutput.size(); ++i) {
                               for (int j = 0; j < expectedOutput[i].size(); ++j) {
                                   std::cout<< expectedOutput[i][j] << " ";
                               }
                               std::cout<< std::endl;
                       }

                       std::cout << endl <<  "result size: " << result.size() << std::endl;
                       for (int i = 0; i < result.size(); ++i) {
                               for (int j = 0; j < result[i].size(); ++j) {
                                   std::cout<< result[i][j] << " ";
                               }
                               std::cout<< std::endl;
                       }

        *
        * */

        /*std::cout<< "Result: " << std::endl;
                      for (int i = 0; i < result.size(); ++i) {
                              for (int j = 0; j < result[i].size(); ++j) {
                                  std::cout<< result[i][j] << " ";
                              }
                              std::cout<< std::endl;
                          }*/
        public String generateFunctionCpp(Hashtable<Integer, Boolean> categoryIds) {
            String inputs = String.join(", ", inputFields.stream()
                           // .filter(field -> !field.getType().startsWith("list<")) //todo: handle checker field
                    .map(field -> mapTypeToCpp(field.getType()) + " " + field.getName())
                    .toArray(String[]::new));

            StringBuilder additionalDataStructureDeclaration = new StringBuilder();

            if (categoryIds != null && categoryIds.containsKey(11) && categoryIds.get(11)) {
                additionalDataStructureDeclaration
                        .append("/*")
                        .append(treeNodeDefinitionCpp)
                        .append("*/")
                        .append("\n");
            }


            return String.format("""
                    %s
                    
                    %s %s(%s) {\n    // Implementation goes here\n    return result;\n}
                    """,
                        additionalDataStructureDeclaration,
                        mapTypeToCpp(outputFields.get(0).getType()),
                        functionName,
                        inputs
            );
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
                case "tree<int>" -> "TreeNode*";
                case "graph<int>" -> "std::vector<std::vector<int>>"; //"std::unordered_map<int, std::vector<int>>"; // Example for graph representation
                default -> "unknown";
            };
        }

        //================================= Java =================================
        public String generateJava(Boolean hasCustomChecker, String additionalCheckerFields, Hashtable<Integer, Boolean> categoryIds) {
            if (hasCustomChecker == null) {
                hasCustomChecker = false;
            }

            String inputReads = inputFields.stream()
                    .map(field -> {
                        if (field.getType().startsWith("list<list<")) {
                            return """
                                    int size_%1$s = scanner.nextInt();
                                    scanner.nextLine(); // Consume the leftover newline character
                                    //Integer.parseInt(scanner.nextLine().trim()); //scanner.nextInt();
                                    
                                    // Đọc từng dòng cho mỗi sublist
                                    List<List<%2$s>> %1$s = new ArrayList<>();
                                    
                                    for (int i = 0; i < size_%1$s; i++) {
                                        if (!scanner.hasNextLine()) {
                                            %1$s.add(new ArrayList<>());
                                            continue;
                                        }
                                        String line = scanner.nextLine().trim();
                                        if (line.trim().isEmpty()) {
                                            %1$s.add(new ArrayList<>());
                                        } else {
                                            String[] tokens = line.split("\\\\s+");
                                            List<%2$s> sublist = new ArrayList<>();
                                            for (String token : tokens) {
                                                sublist.add(%2$s.valueOf(token));
                                            }
                                            %1$s.add(sublist);
                                        }
                                    }
                                    """.formatted(field.getName(), mapTypeToJava(field.getType().replace("list<list<", "").replace(">>", "")));
                        }
                        if (field.getType().startsWith("list<string>")) {
                            return """
                                    int size_%1$s = scanner.nextInt();
                                    
                                    // Đọc dòng tiếp theo chứa các chuỗi
                                    String[] tokens = sc.nextLine().split(" ");
                            
                                    // Lưu vào List<String>
                                    %2$s %1$s = new ArrayList<>();
                                    for (int i = 0; i < size_%1$s; i++) {
                                        %1$s.add(tokens[i]);
                                    }
                                    """.formatted(field.getName(), mapTypeToJava(field.getType()));
                        }
                        if (field.getType().startsWith("list<")) {
                            return """
                                    int size_%1$s = scanner.nextInt();
                                   
                                    %2$s %1$s = new ArrayList<>();
                                    for (int i = 0; i < size_%1$s; i++) {
                                        %1$s.add(scanner.next%3$s());
                                    }
                                    """.formatted(field.getName(), mapTypeToJava(field.getType()), mapScannerMethodForJava(field.getType()));
                        } else if (field.getType().startsWith("graph<int")) {
                            return """
                                    int size_%1$s = scanner.nextInt();
                                    List<List<Integer>> %1$s = new ArrayList<>();
                                    for (int i = 0; i < size_%1$s; i++) {
                                        int size_sublist = scanner.nextInt();
                                        List<Integer> sublist = new ArrayList<>();
                                        for (int j = 0; j < size_sublist; j++) {
                                            sublist.add(scanner.nextInt());
                                        }
                                        %1$s.add(sublist);
                                    }
                                    """.formatted(field.getName());
                        } else if (field.getType().startsWith("tree<")) {
                            return """
                                    String treeInput = scanner.nextLine();
                                    String[] nodes = treeInput.split(" ");
                                    ArrayList<String> nodeList = new ArrayList<>(Arrays.asList(nodes));
                                    %s %s = (new Main().new TreeBuilder()).buildTree(nodes);
                                    """.formatted(mapTypeToJava(field.getType()), field.getName());
                        } else {
                            return "%s %s = scanner.next%s();".formatted(mapTypeToJava(field.getType()), field.getName(), mapScannerMethodForJava(field.getType()));
                        }
                    }).collect(Collectors.joining("\n    "));

            String functionCall = "%s result = (new Main().new Solution()).%s(%s);".formatted(
                    mapTypeToJava(outputFields.get(0).getType()),
                    functionName,
                    inputFields.stream()
                            .filter(field -> !field.getName().contains(EXPECTED_OUTPUT_FIELD_NAME)) //todo: handle checker field
                            .map(Field::getName)
                            .collect(Collectors.joining(", "))
            );

            String outputWrite = null;

            if (outputFields.get(0).getType().startsWith("list<list<")) {
                outputWrite = """
                        for(List<?> sublist : result) {
                                        System.out.println(sublist);
                                    }
                        """;
            } else if (outputFields.get(0).getType().startsWith("list<")) {
                outputWrite = "System.out.println(result.stream().map(Object::toString).collect(Collectors.joining(\" \")));\n";
            } else {
                // For single value output
                outputWrite = "System.out.println(result);\n";
            }

            if (hasCustomChecker) { // && outputFields.get(0).getType().startsWith("list<") .new Solution()
                String functionCustomCheckerCall = "boolean isPassed = (new Main()).customChecker(result, expectedOutput";

                if (additionalCheckerFields != null && !additionalCheckerFields.isEmpty()) {
                    String[] additionalFields = additionalCheckerFields.split(",");
                    String functionCallWithAdditionalFields = String.join(", ", additionalFields);

                    if (!functionCallWithAdditionalFields.isEmpty()) {
                        functionCustomCheckerCall += ", " + functionCallWithAdditionalFields;
                    }
                }

                outputWrite = functionCustomCheckerCall + ");\n" + outputWrite +
                        """
                        if (isPassed) {
                            System.out.println("true");
                        } else {
                            System.out.println("false");
                        }
                        """;
            }

            StringBuilder additionalDataStructureDeclaration = new StringBuilder();

            if (categoryIds != null && categoryIds.containsKey(11) && categoryIds.get(11)) {
                additionalDataStructureDeclaration
                        .append(treeNodeDefinitionJava)
                        .append("\n")
                        .append(treeBuilderDefinitionJava);
            }

            return """
                    import java.util.*;
                    import java.util.stream.Collectors;
                    

                    public class Main {
                        %s

                        ##USER_CODE_HERE##
                        
                        public static void main(String[] args) {
                            Scanner scanner = new Scanner(System.in);
                            %s
                            %s
                            %s
                        }
                    }
                            """.formatted(
                                additionalDataStructureDeclaration,
                                inputReads,
                                functionCall,
                                outputWrite
                        );
        }

        /*
        * System.out.println("Expected Output:");
                            for (List<Integer> innerList : expectedOutput) {
                                        for (Integer element : innerList) {
                                            System.out.print(element + " ");
                                        }
                                        System.out.println(); // New line after each inner list
                                    }

                            System.out.println("Actual Output:");
                            for (List<Integer> innerList : result) {
                                        for (Integer element : innerList) {
                                            System.out.print(element + " ");
                                        }
                                        System.out.println(); // New line after each inner list
                                    }

                             //System.out.println("Actual Checker:" + isPassed);
        * */

        public String generateFunctionJava(Hashtable<Integer, Boolean> categoryIds) {
            StringBuilder javaCode = new StringBuilder();

            // Generate method signature
            String inputs = inputFields.stream()
                    .map(field -> mapTypeToJava(field.getType()) + " " + field.getName())
                    .collect(Collectors.joining(", "));
            String outputType = mapTypeToJava(outputFields.get(0).getType());

            StringBuilder additionalDataStructureDeclaration = new StringBuilder();

            if (categoryIds != null && categoryIds.containsKey(11) && categoryIds.get(11)) {
                additionalDataStructureDeclaration
                        .append("/*")
                        .append(treeNodeDefinitionJava)
                        .append("*/")
                        .append("\n");
            }

            javaCode.append(String.format("""
                    %s
                    public class Solution {
                        public %s %s(%s) {\n
                            // Implementation goes here
                            return null;\n
                        }
                    }
                    """,
                    additionalDataStructureDeclaration,
                    outputType,
                    functionName,
                    inputs)
            );
            /*javaCode.append("    // Implementation goes here\n");
            javaCode.append("    return null;\n");
            javaCode.append("}\n");*/

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
                case "int" -> "Integer";
                case "float" -> "Double"; // Java uses double for floating-point numbers by default
                case "string" -> "String";
                case "bool" -> "Boolean";
                case "list<int>" -> "List<Integer>";
                case "list<float>" -> "List<Double>";
                case "list<string>" -> "List<String>";
                case "list<bool>" -> "List<Boolean>";
                case "tree<int>" -> "Main.TreeNode";
                case "graph<int>" -> "std::vector<std::vector<int>>";
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
        public String generatePython(Boolean hasCustomChecker, String additionalCheckerFields, Hashtable<Integer, Boolean> categoryIds) {
            if (hasCustomChecker == null) {
                hasCustomChecker = false;
            }

            String inputReads = inputFields.stream()
                    .map(field -> {
                        if (field.getType().startsWith("list<list<string")) {
                            return """
                                    size%1$s = int(input())
                                        %1$s = [input().split() for _ in range(size%1$s)]
                                    """.formatted(field.getName());
                        } else if (field.getType().startsWith("list<list<int")) {
                            return """
                                    size%1$s = int(input())
                                        %1$s = [list(map(int, input().split())) for _ in range(size%1$s)]
                                    """.formatted(field.getName());
                        } else if (field.getType().startsWith("list<list<")) {
                            return """
                                    size%1$s = int(input())
                                        %1$s = [list(map(%2$s, input().split())) for _ in range(size%1$s)]
                                    """.formatted(field.getName(), mapTypeToPython(field.getType().substring(10, field.getType().length() - 2)));
                        } else if (field.getType().startsWith("list<string>")) {
                            return """
                                    size%1$s = int(input())
                                        #%1$s = [input().strip() for _ in range(size%1$s)]
                                        %1$s = input().strip().split()[:size%1$s]
                                    """.formatted(field.getName());
                        } else if (field.getType().startsWith("list<bool>")) {
                            return """
                                    size%1$s = int(input())
                                        %1$s = list(map(lambda x: x.lower() == 'true', input().split()[:size%1$s]))
                                    """.formatted(field.getName());
                        } else if (field.getType().startsWith("list<")) {
                            return """
                                    size%1$s = int(input())
                                        %1$s = list(map(%2$s, input().split()[:size%1$s]))
                                    """.formatted(field.getName(), mapTypeToPython(field.getType()));
                        } else if (field.getType().startsWith("tree<")) {
                            return """
                                    nodes = input().split()  # Read the list of strings
                                        %1$s = build_tree(nodes)
                                    """.formatted(field.getName());
                        } else if (field.getType().startsWith("graph<int")) {
                            return """
                                    size_%1$s = int(input())
                                        %1$s = [list(map(int, input().split())) for _ in range(size_%1$s)]
                                    """.formatted(field.getName());
                        } else {
                            return "%s = %s(input())".formatted(field.getName(), mapTypeToPython(field.getType()));
                        }
                    }).collect(Collectors.joining("\n    "));

            String functionCall = "result = %s(%s)".formatted(
                    functionName,
                    inputFields.stream()
                            .filter(field -> !field.getName().contains(EXPECTED_OUTPUT_FIELD_NAME)) //todo: handle checker field
                            .map(Field::getName)
                            .collect(Collectors.joining(", "))
            );
            String outputWrite = null;
            if (outputFields.get(0).getType().startsWith("list<list<bool>>")) {
                outputWrite = "for sublist in result:\n    print(' '.join(['true' if item else 'false' for item in sublist]))\n";
            } else if (outputFields.get(0).getType().startsWith("list<list<")) {
                outputWrite = "for sublist in result: print(sublist)\n";
            } else if (outputFields.get(0).getType().startsWith("list<bool>")) {
                outputWrite = "print(' '.join(['true' if item else 'false' for item in result]))\n";
            } else if (outputFields.get(0).getType().startsWith("list<")) {
                outputWrite = "print(' '.join(map(str, result)))\n";
            } else if (outputFields.get(0).getType().startsWith("bool")) {
                outputWrite = "print('true\\n' if result else 'false\\n')\n";
            } else {
                outputWrite = "print(result)\n";
            }

            if (hasCustomChecker) { // && outputFields.get(0).getType().startsWith("list<")
                String functionCustomCheckerCall = "isPassed = customChecker(result, expectedOutput";

                if (additionalCheckerFields != null && !additionalCheckerFields.isEmpty()) {
                    String[] additionalFields = additionalCheckerFields.split(",");
                    String functionCallWithAdditionalFields = String.join(", ", additionalFields);

                    if (!functionCallWithAdditionalFields.isEmpty()) {
                        functionCustomCheckerCall += ", " + functionCallWithAdditionalFields;
                    }
                }

                outputWrite = functionCustomCheckerCall + ")\n" + outputWrite +
                        """
                        if isPassed:
                            print("true")
                        else:
                            print("false")
                        """;
            }

            StringBuilder additionalDataStructureDeclaration = new StringBuilder();
            if (categoryIds != null && categoryIds.containsKey(11) && categoryIds.get(11)) {
                additionalDataStructureDeclaration
                        .append(treeNodeDefinitionPython)
                        .append("\n")
                        .append(treeBuilderDefinitionPython);
            }

            return """
                    import sys
                    from typing import List, Any, Union, Tuple
                    from collections import defaultdict
                    ##USER_CODE_HERE##
                  
                    %s

                    if __name__ == '__main__':
                        %s
                        %s
                        
                        %s
                            """.formatted(additionalDataStructureDeclaration, inputReads, functionCall, outputWrite);
        }

        /*

      print("size candidates:", sizecandidates)
                        print("candidates:", candidates)
                        print("target:", target)
                        print("size expected output:", sizeexpectedOutput)
                        print("expected output:", expectedOutput)
        * */

        public String generateFunctionPython(Hashtable<Integer, Boolean> categoryIds) {
            StringBuilder pythonCode = new StringBuilder();

            // Generate method signature
            String inputs = inputFields.stream()
                    .map(Field::getName)
                    .collect(Collectors.joining(", "));

            StringBuilder additionalDataStructureDeclaration = new StringBuilder();

            if (categoryIds != null && categoryIds.containsKey(11) && categoryIds.get(11)) {
                additionalDataStructureDeclaration
                        .append("'''\n")
                        .append(treeNodeDefinitionPython)
                        .append("'''")
                        .append("\n");
            }

            pythonCode.append(String.format("""
                    import sys
                    
                    %s
                    
                    def %s(%s):\n
                    """, additionalDataStructureDeclaration, functionName, inputs));
            pythonCode.append("    # Implementation goes here\n");
            pythonCode.append("    return None\n");

            return pythonCode.toString();
        }

        private String mapTypeToPython(String type) {

            return switch (type) {
                case "int", "list<int>" -> "int";
                case "float" -> "float";
                case "string" -> "str";
                case "bool"-> "bool"; // Note: Boolean parsing may require additional handling
                case "list<list<int>>" -> "list[list[int]]";
                case "list<list<float>>" -> "list[list[float]]";
                case "list<list<string>>" -> "list[list[str]]";
                case  "list<float>" -> "list[float]";
                case  "list<string>" -> "list[str]";
                case  "list<bool>" -> "list[bool]";
                /*case "tree<int>" -> "TreeNode"; // Assuming TreeNode is defined in the user code
                case "graph<int>" -> "List[List[int]]"; // Example for graph representation
                case "list<int>" -> "list[int]";*/
                default -> "str"; // Default to string for unknown types
            };
        }

        //============================== Java Script ==============================================
        public String generateJavaScript(Boolean hasCustomChecker, String additionalCheckerFields, Hashtable<Integer, Boolean> categoryIds) {
            if (hasCustomChecker == null) {
                hasCustomChecker = false;
            }

            String inputReads = inputFields.stream()
                    .map(field -> {
                        if (field.getType().startsWith("list<list<")) {
                            return """
                                    const size_%1$s = Number(input[nextLine++]);
                                    const %1$s = [];
                                    for (let i = 0; i < size_%1$s; i++) {
                                        const line = input[nextLine++];
                                        if (!line || line.trim() === '') {
                                            listOfLists.push([]);
                                        }
                                        const sublist = line.trim().split(/\\s+/).map(%2$s);
                                        %1$s.push(sublist);
                                    }
                                    """.formatted(field.getName(), mapTypeToJavaScript(field.getType().replace("list<", "").replace(">", "")));
                        } else if (field.getType().startsWith("list<")) {
                            return """
                                    const size_%1$s = Number(input[nextLine++]);
                                    const %1$s = input[nextLine++].trim().split(/\\s+/).map(%2$s);
                                    """.formatted(field.getName(), mapTypeToJavaScript(field.getType()));
                        } else if (field.getType().startsWith("tree<")) {
                            return """
                                    const nodes = input[nextLine++].trim().split(/\\s+/);
                                    const %1$s = buildTree(nodes);
                                    """.formatted(field.getName());
                        } else if (field.getType().startsWith("graph<")) {
                            return """
                                    const size_%1$s = Number(input[nextLine++]);
                                    const %1$s = [];
                                    for (let i = 0; i < size_%1$s; i++) {
                                        const line = input[nextLine++];
                                        if (!line || line.trim() === '') {
                                            %1$s.push([]);
                                        } else {
                                            const neighbors = line.trim().split(/\\s+/).map(Number);
                                            %1$s.push(neighbors);
                                        }
                                    }
                                    """.formatted(field.getName());
                        } else if (field.getType().startsWith("str")) {
                            return "const %s = input[nextLine++];".formatted(field.getName());
                        } else if (field.getType().startsWith("bool")) {
                            return "const %s = input[nextLine++] === 'true';".formatted(field.getName());

                        } else {
                            return "const %s = %s(input[nextLine++]);".formatted(field.getName(), mapTypeToJavaScript(field.getType()));
                        }
                    }).collect(Collectors.joining("\n  "));

            String functionCall = "const result = %s(%s);".formatted(
                    functionName,
                    inputFields.stream()
                            .filter(field -> !field.getName().contains(EXPECTED_OUTPUT_FIELD_NAME)) //todo: handle checker field
                            .map(Field::getName)
                            .collect(Collectors.joining(", "))
            );
            String outputWrite = null;// "console.log(result);";

            if (outputFields.get(0).getType().startsWith("list<list<")) {
                outputWrite = "result.forEach(sublist => {console.log(`[${sublist.join(\", \")}]`);});\n";
            } else if (outputFields.get(0).getType().startsWith("list<")) {
                outputWrite = "console.log(result.join(' '));\n";
            } else {
                outputWrite = "console.log(result);\n";
            }

            if (hasCustomChecker) { // && outputFields.get(0).getType().startsWith("list<")
                String functionCustomCheckerCall = "const isPassed = customChecker(result, expectedOutput";

                if (additionalCheckerFields != null && !additionalCheckerFields.isEmpty()) {
                    String[] additionalFields = additionalCheckerFields.split(",");
                    String functionCallWithAdditionalFields = String.join(", ", additionalFields);

                    if (!functionCallWithAdditionalFields.isEmpty()) {
                        functionCustomCheckerCall += ", " + functionCallWithAdditionalFields;
                    }
                }

                outputWrite = functionCustomCheckerCall + ");\n" + outputWrite +
                        """
                        if (isPassed) {
                            console.log("true");
                        } else {
                            console.log("false");
                        }
                        """;
            }

            StringBuilder additionalDataStructureDeclaration = new StringBuilder();
            if (categoryIds != null && categoryIds.containsKey(11) && categoryIds.get(11)) {
                additionalDataStructureDeclaration
                        .append(treeNodeDefinitionJavascript)
                        .append("\n")
                        .append(treeBuilderDefinitionJavascript);
            }

            return """
                    %s
                    
                    ##USER_CODE_HERE##

                    const readline = require('readline');
                    
                    const rl = readline.createInterface({
                         input: process.stdin,
                         output: process.stdout,
                    });
                    
                    let input = [];
                   
                    rl.on('line', (line) => {
                         input.push(line);
                    }).on('close', () => {
                        let nextLine = 0;
                        %s
                        %s
                        %s
                    });
                            """.formatted(
                                    additionalDataStructureDeclaration,
                        inputReads,
                        functionCall,
                        outputWrite
            );
        }

        public String generateFunctionJavaScript(Hashtable<Integer, Boolean> categoryIds) {
            StringBuilder jsCode = new StringBuilder();

            // Generate function signature
            String inputs = inputFields.stream()
                    .map(Field::getName)
                    .collect(Collectors.joining(", "));

            StringBuilder additionalDataStructureDeclaration = new StringBuilder();

            if (categoryIds != null && categoryIds.containsKey(11) && categoryIds.get(11)) {
                additionalDataStructureDeclaration
                        .append("/*")
                        .append(treeNodeDefinitionJavascript)
                        .append("*/")
                        .append("\n");
            }

            jsCode.append(String.format("""
                    %s
                    function %s(%s) {
                    """, additionalDataStructureDeclaration, functionName, inputs));

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
                /*case "list<list<int>>" -> "Array";
                case "list<list<float>>" -> "Array";
                case "list<list<string>>" -> "Array";*/
                /*case "tree<int>" -> "TreeNode"; // Assuming TreeNode is defined in the user code
                case "graph<int>" -> "Array"; // Example for graph representation
                case "list<bool>" -> "Array";
                case "list<list<bool>>" -> "Array";*/
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

        public static String defaultCodeGenerator(String structure, int languageId, Hashtable<Integer, Boolean> categoryIds) {
            BoilerPlateGenerator parser = new BoilerPlateGenerator();
            parser.parse(structure, false); // dont add the expected output field into the partial boilerplate
//            System.out.println(structure);
//            System.out.println(parser.functionName);
            return switch (languageId) {
                case 48, 49, 50 -> parser.generateFunctionC();
                case 51 -> parser.generateFunctionCSharp();
                case 52, 53, 54 -> parser.generateFunctionCpp(categoryIds);
                case 62, 91 -> parser.generateFunctionJava(categoryIds);
                case 63 -> parser.generateFunctionJavaScript(categoryIds);
                case 71 -> parser.generateFunctionPython(categoryIds);
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
    public String enrich(
            String code, int languageId,
            String structure, Boolean hasCustomChecker,
            String additionalCheckerFields,
            Hashtable<Integer, Boolean> categoryIds
    ) {
        if (hasCustomChecker == null) {
            hasCustomChecker = false;
        }

        BoilerPlateGenerator parser = new BoilerPlateGenerator();
        parser.parse(structure, hasCustomChecker);



        String enrichBoilerPlate = switch (languageId) {
            case 48, 49, 50 -> parser.generateC();
            case 51 -> parser.generateCSharp();
            case 52, 53, 54 -> parser.generateCpp(hasCustomChecker, additionalCheckerFields, categoryIds);
            case 62, 91 -> parser.generateJava(hasCustomChecker, additionalCheckerFields, categoryIds);
            case 63 -> parser.generateJavaScript(hasCustomChecker, additionalCheckerFields, categoryIds);
            case 71 -> parser.generatePython(hasCustomChecker, additionalCheckerFields, categoryIds);
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

    private static String extractPythonFunction(String code, String functionName) {
        List<String> functions = new ArrayList<>();

        Pattern pattern = Pattern.compile(
                "(?m)^def\\s+[\\w_]+\\s*\\([^\\)]*\\):\\s*\\n((^[ \\t]+.*\\n?)*)"
        );

        Matcher matcher = pattern.matcher(code);
        while (matcher.find()) {
            functions.add(matcher.group(0)); // Toàn bộ hàm gồm cả 'def' và thân
        }

        return String.join("\n", functions);
    }


    public static String extractBraceBasedFunction(String code, String language) {
        List<String> functions = new ArrayList<>();
        // Regex tìm tất cả hàm có block {}
        Pattern pattern = Pattern.compile(
                "(?s)([\\w\\s<>\\[\\],]+\\s+(\\w+)\\s*\\([^)]*\\)\\s*\\{)"
        );
        Matcher matcher = pattern.matcher(code);
        while (matcher.find()) {
            String header = matcher.group(1);
            String name = matcher.group(2);
            if (name.equals("main")) continue;

            // Đếm {} để lấy toàn bộ thân hàm
            int start = matcher.start();
            int i = matcher.end();
            int openBraces = 1;
            while (i < code.length() && openBraces > 0) {
                char c = code.charAt(i++);
                if (c == '{') openBraces++;
                else if (c == '}') openBraces--;
            }
            String fullFunction = code.substring(start, i);
            functions.add(fullFunction);
        }

        return String.join("\n", functions);
    }

    public String extractFunctionCode(String code, String language, String structure) {
        BoilerPlateGenerator parser = new BoilerPlateGenerator();
        parser.parse(structure, false); // false to not include expected output field


        String functionName = parser.functionName;
        String normalLanguage = normalizeLanguage(language);
        return switch (normalLanguage.toLowerCase()) {
            case "c", "cpp", "c++", "java", "js", "ts", "c#", "csharp", "javascript" ->
                    extractBraceBasedFunction(code, language);
            case "python" -> extractPythonFunction(code, functionName);
            default -> throw new IllegalArgumentException("Unsupported language: " + normalLanguage);
        };
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