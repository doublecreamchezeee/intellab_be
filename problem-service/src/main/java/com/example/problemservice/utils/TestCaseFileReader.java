package com.example.problemservice.utils;

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

public class TestCaseFileReader {
    public static final String INPUT = "inputs";
    public static final String OUTPUT = "outputs";
    private static final String MOUNT_PATH = "problems";

    public static List<String> getProblemTestCases(String problemId, String testType) {
        try {
            List<Path> files = Files.list(Paths.get(MOUNT_PATH, slugify(problemId), "tests", testType))
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
                List<Path> files = Files.list(Paths.get(MOUNT_PATH, problemId, "tests", "inputs"))
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
                List<Path> files = Files.list(Paths.get(MOUNT_PATH, problemId, "tests", "outputs"))
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
            Path testsDir = Paths.get(MOUNT_PATH, slugify(problemId), "tests");
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
}
