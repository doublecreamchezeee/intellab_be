package com.example.problemservice.service;

import com.example.problemservice.model.Problem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

@Slf4j
@Service
public class MarkdownService {

    public void saveProblemAsMarkdown(Problem problem) {

        try {
            String markdownContent = convertProblemToMarkdown(problem);
            saveMarkdownToFile(problem.getProblemName(), markdownContent, "Problem.md");

            String solutionStructureMarkdownContent = convertSolutionStructureToMarkdown(problem);
            saveMarkdownToFile(problem.getProblemName(), solutionStructureMarkdownContent, "Structure.md");

        } catch (IOException e) {
            log.error("Failed to save problem as markdown: ", e);
        }
    }

    private String convertProblemToMarkdown(Problem problem) {
        StringBuilder sb = new StringBuilder();
        sb.append("# ").append(problem.getProblemName()).append("\n\n");

        sb.append("## Level\n");
        sb.append(problem.getProblemLevel()).append("\n\n");

        sb.append("## Description\n");
        sb.append(problem.getDescription()).append("\n\n");
        return sb.toString();
    }

    private String convertSolutionStructureToMarkdown(Problem problem) {
        StringBuilder sb = new StringBuilder();
        sb.append("## Solution Structure\n");
        sb.append(problem.getSolutionStructure()).append("\n\n");
        return sb.toString();
    }

    private void saveMarkdownToFile(String problemName, String markdownContent, String filename) throws IOException {
        Path path = Paths.get("problems", problemName, filename);
        Files.createDirectories(path.getParent());
        try (FileWriter writer = new FileWriter(path.toFile())) {
            writer.write(markdownContent);
        } catch (IOException e) {
            log.error("Failed to save markdown to file: ", e);

        }
    }

    public void deleteProblemFolder(String problemName) {
        Path path = Paths.get("problems", problemName);
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
            log.info("Deleted problem folder: " + path);
        } catch (IOException e) {
            log.error("Failed to delete problem folder: " + path, e);
        }
    }
}
