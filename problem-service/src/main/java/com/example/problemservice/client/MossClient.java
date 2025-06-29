package com.example.problemservice.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class MossClient {

    /**
     * Run MOSS check on code snippets.
     * @param codeSnippets list of code content strings
     * @param language e.g., "java"
     * @param baseCode optional base code, can be null
     * @return MOSS report URL
     */
    public String runMoss(List<String> codeSnippets, String language, String baseCode) throws IOException, InterruptedException {

        // Use system temp directory for dynamic path
        String systemTemp = System.getProperty("java.io.tmpdir");
        File baseTempDir = new File(systemTemp, "moss-temp");

        if (!baseTempDir.exists()) {
            if (!baseTempDir.mkdirs()) {
                throw new IOException("Could not create base temp directory: " + baseTempDir.getAbsolutePath());
            }
        }

        // Create unique run folder
        String runId = UUID.randomUUID().toString();
        File runDir = new File(baseTempDir, runId);
        if (!runDir.mkdirs()) {
            throw new IOException("Could not create run temp folder: " + runDir.getAbsolutePath());
        }
        log.info("Created temp folder: {}", runDir.getAbsolutePath());

        List<File> codeFiles = new ArrayList<>();
        File baseFile = null;

        // Save code files
        int i = 1;
        for (String content : codeSnippets) {
            File f = new File(runDir, "Submission" + i + ".java");
            Files.writeString(f.toPath(), content);
            codeFiles.add(f);
            i++;
        }

        // Save base file if exists
        if (baseCode != null) {
            baseFile = new File(runDir, "Base.java");
            Files.writeString(baseFile.toPath(), baseCode);
        }

        // Build Docker command
        List<String> cmd = new ArrayList<>();
        cmd.add("docker");
        cmd.add("run");
        cmd.add("--rm");
        cmd.add("-v");
        // Host path : Container path
        cmd.add(runDir.getAbsolutePath() + ":/data");
        cmd.add("moss-service");
        cmd.add("-l");
        cmd.add(language);
        if (baseFile != null) {
            cmd.add("-b");
            cmd.add("/data/" + baseFile.getName());
        }
        for (File f : codeFiles) {
            cmd.add("/data/" + f.getName());
        }

        log.info("Running MOSS Docker: {}", String.join(" ", cmd));

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        String reportUrl = null;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                log.info("[MOSS] {}", line);
                if (line.startsWith("http")) {
                    reportUrl = line.trim();
                }
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("MOSS Docker exited with code " + exitCode);
        }

        if (reportUrl == null) {
            throw new RuntimeException("Could not parse MOSS report URL");
        }

        // Optional cleanup (or keep for debugging)
        deleteDirectory(runDir);

        return reportUrl;
    }

    /**
     * Recursively delete directory
     */
    private void deleteDirectory(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteDirectory(f);
                } else {
                    f.delete();
                }
            }
        }
        dir.delete();
    }
}
