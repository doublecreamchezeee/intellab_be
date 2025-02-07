package com.example.problemservice.utils;

import com.example.problemservice.configuration.AppConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TestCaseFileReader {
    public static final String INPUT = "inputs";
    public static final String OUTPUT = "outputs";

    /*@Value("${mount_path}")
    public static String MOUNT_PATH; // = "problems";*/

    private static AppConfig appConfig;

    @Autowired
    public TestCaseFileReader(AppConfig appConfig) {
        TestCaseFileReader.appConfig = appConfig;
    }

    public static List<String> getProblemTestCases(String problemId, String testType) {
        try {
            List<Path> files = Files.list(Paths.get(
                            appConfig.getMountPath(),
                            slugify(problemId),
                            "tests", testType))
                    .collect(Collectors.toList());
            return files.stream()
                    .map(file -> {
                        try {
                            return Files.readString(file);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static CompletableFuture<List<String>> getProblemInputs(String problemId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<Path> files = Files.list(Paths.get(
                                appConfig.getMountPath(),
                                problemId,
                                "tests",
                                "inputs"))
                        .collect(Collectors.toList());
                List<CompletableFuture<String>> futures = new ArrayList<>();
                for (Path file : files) {
                    futures.add(CompletableFuture.supplyAsync(() -> {
                        try {
                            return Files.readString(file);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }));
                }
                return futures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static CompletableFuture<List<String>> getProblemOutputs(String problemId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<Path> files = Files.list(Paths.get(
                        appConfig.getMountPath(),
                                problemId,
                                "tests",
                                "outputs"))
                        .collect(Collectors.toList());
                List<CompletableFuture<String>> futures = new ArrayList<>();
                for (Path file : files) {
                    futures.add(CompletableFuture.supplyAsync(() -> {
                        try {
                            return Files.readString(file);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }));
                }
                return futures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static void saveTestCases(String problemId, List<String> inputs, List<String> outputs) {
        if (inputs.size() != outputs.size()) {
            throw new IllegalArgumentException("Inputs and outputs lists must have the same size");
        }

        try {
            Path testsDir = Paths.get(
                    appConfig.getMountPath(),
                    slugify(problemId),
                    "tests");
            Path inputDir = testsDir.resolve(INPUT);
            Path outputDir = testsDir.resolve(OUTPUT);

            Files.createDirectories(testsDir);
            Files.createDirectories(inputDir);
            Files.createDirectories(outputDir);

            for (int i = 0; i < inputs.size(); i++) {
                Path inputPath = inputDir.resolve(i + ".txt");
                Path outputPath = outputDir.resolve(i + ".txt");

                Files.writeString(inputPath, inputs.get(i));
                Files.writeString(outputPath, outputs.get(i));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to save test cases", e);
        }
    }

    public static String slugify(String input) {
        String nonWhitespace = input.trim().replaceAll("\\s+", "-");
        String normalized = Normalizer.normalize(nonWhitespace, Normalizer.Form.NFD);
        String slug = normalized.replaceAll("[^\\w-]", "").toLowerCase(Locale.ENGLISH);
        return slug;
    }

    public static void saveOneTestCase(String problemId, String input, String output) {
        try {
            Path testsDir = Paths.get(
                    appConfig.getMountPath(),
                    slugify(problemId),
                    "tests");
            Path inputDir = testsDir.resolve(INPUT);
            Path outputDir = testsDir.resolve(OUTPUT);

            Files.createDirectories(testsDir);
            Files.createDirectories(inputDir);
            Files.createDirectories(outputDir);

            long inputCount = Files.list(inputDir).count();
            long outputCount = Files.list(outputDir).count();

            long newIndex = Math.max(inputCount, outputCount);

            Path inputPath = inputDir.resolve(newIndex + ".txt");
            Path outputPath = outputDir.resolve(newIndex + ".txt");

            Files.writeString(inputPath, input);
            Files.writeString(outputPath, output);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save one test case", e);
        }
    }

}
