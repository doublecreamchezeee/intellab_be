package com.example.problemservice.utils;

import com.example.problemservice.configuration.AppConfig;
import com.example.problemservice.model.Problem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.Normalizer;
import java.util.Locale;

@Slf4j
@Component
public class MarkdownUtility {

   /* @Value("${mount_path}")
    public static String MOUNT_PATH; // = "problems";*/

    private static AppConfig appConfig;

    @Autowired
    public MarkdownUtility(AppConfig appConfig) {
        MarkdownUtility.appConfig = appConfig;
    }

    public static void saveProblemAsMarkdown(Problem problem) {

        try {
            String markdownContent = convertProblemToMarkdown(problem);
            saveMarkdownToFile(problem.getProblemName(), markdownContent, "Problem.md");

            String solutionStructureMarkdownContent = convertProblemStructureToMarkdown(problem);
            saveMarkdownToFile(problem.getProblemName(), solutionStructureMarkdownContent, "Structure.md");

        } catch (IOException e) {
            log.error("Failed to save problem as markdown: ", e);
        }
    }

    private static String convertProblemToMarkdown(Problem problem) {
        StringBuilder sb = new StringBuilder();
        sb.append("# ").append(problem.getProblemName()).append("\n\n");

        sb.append("## Level\n");
        sb.append(problem.getProblemLevel()).append("\n\n");

        sb.append("## Description\n");
        sb.append(problem.getDescription()).append("\n\n");
        return sb.toString();
    }

    private static String convertProblemStructureToMarkdown(Problem problem) {
        StringBuilder sb = new StringBuilder();
        //sb.append("## Solution Structure\n");
        sb.append(problem.getProblemStructure()).append("\n\n");
        return sb.toString();
    }

    private static void saveMarkdownToFile(String problemName, String markdownContent, String filename) throws IOException {
        log.info("Mount Path: " + appConfig.getMountPath());
        Path path = Paths.get(appConfig.getMountPath(), slugify(problemName), filename);
        log.info("Saving markdown to file: " + path.toAbsolutePath());
        Files.createDirectories(path.getParent());
        try (FileWriter writer = new FileWriter(path.toFile())) {
            writer.write(markdownContent);
        } catch (IOException e) {
            log.error("Failed to save markdown to file: ", e);

        }
    }

    public static void deleteProblemFolder(String problemName) {
        Path path = Paths.get(appConfig.getMountPath(), slugify(problemName));
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

    public static String readMarkdownFromFile(String problemName, String filename) {
        Path path = Paths.get(appConfig.getMountPath(), slugify(problemName), filename);
        try {
            return Files.readString(path);
        } catch (IOException e) {
            log.error("Failed to read markdown from file: ", e);
            return null;
        }
    }

    public static String slugify(String input) {
        String nonWhitespace = input.trim().replaceAll("\\s+", "-");
        String normalized = Normalizer.normalize(nonWhitespace, Normalizer.Form.NFD);
        String slug = normalized.replaceAll("[^\\w-]", "").toLowerCase(Locale.ENGLISH);
        return slug;
    }
    
}
