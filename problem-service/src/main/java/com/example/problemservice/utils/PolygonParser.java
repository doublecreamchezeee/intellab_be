package com.example.problemservice.utils;

import com.example.problemservice.dto.PolygonProblemData;
import com.example.problemservice.dto.PolygonTestCase;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class PolygonParser {

    public static PolygonProblemData parse(File problemXmlFile, File statementsDir, File testsDir) {
        try {
            PolygonProblemData data = new PolygonProblemData();

            // Parse problem.xml
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(problemXmlFile);

            Element root = doc.getDocumentElement();

            data.setTitle(root.getAttribute("name"));

            // Parse statement HTML
            File htmlStatement = new File(statementsDir, "english/problem.html");
            if (htmlStatement.exists()) {
                data.setDescriptionHtml(Files.readString(htmlStatement.toPath()));
            }

            // Load test cases
            List<PolygonTestCase> testCases = new ArrayList<>();
            File[] inputFiles = new File(testsDir, "tests").listFiles((dir, name) -> name.endsWith(".in"));

            if (inputFiles != null) {
                for (File input : inputFiles) {
                    String name = input.getName().replace(".in", "");
                    File output = new File(testsDir, "tests/" + name + ".out");

                    if (output.exists()) {
                        String inText = Files.readString(input.toPath());
                        String outText = Files.readString(output.toPath());
                        testCases.add(new PolygonTestCase(inText.trim(), outText.trim()));
                    }
                }
            }

            data.setTestCases(testCases);

            // Parse solution
            NodeList solutionNodes = root.getElementsByTagName("solution");
            for (int i = 0; i < solutionNodes.getLength(); i++) {
                Element solutionElement = (Element) solutionNodes.item(i);
                String type = solutionElement.getAttribute("type");
                String path = solutionElement.getAttribute("path");

                // Only take the main solution
                if ("main".equalsIgnoreCase(type)) {
                    File solutionFile = new File(problemXmlFile.getParentFile(), path);
                    if (solutionFile.exists()) {
                        data.setSolutionCode(Files.readString(solutionFile.toPath()));
                        // Infer language by file extension
                        if (path.endsWith(".cpp")) data.setSolutionLanguage("cpp");
                        else if (path.endsWith(".java")) data.setSolutionLanguage("java");
                        else if (path.endsWith(".py")) data.setSolutionLanguage("python");
                        else data.setSolutionLanguage("unknown");
                    }
                    break;
                }
            }

            return data;

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Polygon problem", e);
        }
    }
}
