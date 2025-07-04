package com.example.problemservice.client;

import com.example.problemservice.dto.response.problemSubmission.MossMatchResponse;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import com.example.problemservice.dto.request.ProblemSubmission.MossRequest;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.Comparator;

@Slf4j
@Service
public class MossClient {

    /**
     * Run MOSS check on code snippets.
     *
     * @param codeSnippets list of code content strings
     * @param language     e.g., "java"
     * @param baseCode     optional base code, can be null
     * @return MOSS report URL
     */
    public String runMoss(List<String> codeSnippets, String language, String baseCode)
            throws IOException, InterruptedException {

        // Use system temp directory for dynamic path
        String systemTemp = System.getProperty("java.io.tmpdir");
        File baseTempDir = new File(systemTemp, "moss-temp");
        String mossLanguage = mapToMossLanguage(language);
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
            File f = new File(runDir, "Submission" + i + "." + mossLanguage);
            Files.writeString(f.toPath(), content);
            codeFiles.add(f);
            i++;
        }

        // Save base file if exists
        if (baseCode != null) {
            baseFile = new File(runDir, "Base." + mossLanguage);
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

    public String moss(List<MossRequest> requests, String language) throws IOException, InterruptedException {

        // Use system temp directory for dynamic path
        String systemTemp = System.getProperty("java.io.tmpdir");
        File baseTempDir = new File(systemTemp, "moss-temp");
        String mossLanguage = mapToMossLanguage(language);
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

        // List<File> codeFiles = new ArrayList<>();
        File baseFile = null;

        // Save code files
        // int i = 1;
        // for (String content : codeSnippets) {
        // File f = new File(runDir, "Submission" + i + "." + mossLanguage);
        // Files.writeString(f.toPath(), content);
        // codeFiles.add(f);
        // i++;
        // }

        List<File> codeFiles = requests.stream().map(request -> {
            File f = new File(runDir,
                    request.getSubmissionId().toString() + "_" +
                            request.getUserId().toString() + "." +
                            mossLanguage);
            try {
                Files.writeString(f.toPath(), request.getFunctionCode());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return f;
        }).toList();

        // Save base file if exists
        // if (baseCode != null) {
        // baseFile = new File(runDir, "Base." + mossLanguage);
        // Files.writeString(baseFile.toPath(), baseCode);
        // }

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

    private String mapToMossLanguage(String normalized) {
        if (normalized == null || normalized.isEmpty()) {
            throw new IllegalArgumentException("Language cannot be null or empty");
        }

        return switch (normalized) {
            case "cpp" -> "cc"; // MOSS expects "cc" for C++
            case "csharp" -> "csharp";
            case "javascript" -> "javascript";
            case "typescript" -> "javascript"; // No "typescript" in MOSS, map to "javascript"
            case "python" -> "python";
            case "java" -> "java";
            case "c" -> "c";
            default -> throw new IllegalArgumentException("Unsupported language for MOSS: " + normalized);
        };
    }

    public String fetchHighlightedCode(String baseUrl, Integer order) throws Exception {
        // Convert .../ -> .../match<i>-1.html

        String codeUrl = baseUrl + "match" + order.toString() + "-1.html";

        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(codeUrl))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to fetch highlighted code: HTTP " + response.statusCode());
        }

        // Parse the HTML and extract the <pre> block inside <body>
        Document doc = Jsoup.parse(response.body());
        Element pre = doc.selectFirst("body pre");
        if (pre != null) {
            return pre.text();
        } else {
            return "";
        }
    }

    public String fetchMossHtml(String url) throws Exception {
        // Ensure trailing slash to avoid redirect in the first place
        if (!url.endsWith("/")) {
            url = url + "/";
        }

        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return response.body();
        } else {
            throw new RuntimeException("Failed to fetch MOSS result: HTTP " + response.statusCode());
        }
    }

    public List<MossMatchResponse> parseMossHtml(String url, String html, String submissionId) {
        Document doc = Jsoup.parse(html);

        Elements rows = doc.select("table tbody tr");
        List<MossMatchResponse> results = new ArrayList<>();

        for (int i = 1; i < rows.size(); i++) { // skip header row
            Element row = rows.get(i);
            Elements cells = row.select("td");

            String file1Text = cells.get(0).text(); // e.g., "/data/Submission1.java (94%)"
            String file2Text = cells.get(1).text();

            int percent = extractPercent(file1Text);

            String file1Name = extractFileName(file1Text);
            String file2Name = extractFileName(file2Text);

            // file name format: <submissionId>_<userId>.<language>
            String[] file1Parts = splitFileName(file1Name);
            String[] file2Parts = splitFileName(file2Name);
            // Only keep rows where submissionId1 matches
            if (!file1Parts[0].equals(submissionId)) {
                continue;
            }

            try {
                String highlightedCode = fetchHighlightedCode(url, i);
                MossMatchResponse result = MossMatchResponse.builder()
                        .submissionId1(file1Parts[0])
                        .userId1(file1Parts[1])
                        .submissionId2(file2Parts[0])
                        .userId2(file2Parts[1])
                        .percent(percent)
                        .matchCode(highlightedCode)
                        .build();
                results.add(result);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return results.stream()
                .sorted(Comparator.comparingInt(MossMatchResponse::getPercent).reversed())
                .limit(3)
                .collect(Collectors.toList());
    }

    private int extractPercent(String text) {
        int start = text.indexOf('(');
        int end = text.indexOf('%');
        if (start >= 0 && end > start) {
            String num = text.substring(start + 1, end).trim();
            return Integer.parseInt(num);
        }
        return 0;
    }

    private String extractFileName(String text) {
        int start = text.lastIndexOf('/');
        int end = text.indexOf('(');
        if (start >= 0 && end > start) {
            return text.substring(start + 1, end).trim();
        }
        return text.trim();
    }

    private String[] splitFileName(String filename) {
        // Example: 53e456ef_3ba3c82c.java
        int underscoreIndex = filename.indexOf('_');
        int dotIndex = filename.indexOf('.');
        if (underscoreIndex >= 0 && dotIndex > underscoreIndex) {
            String submissionId = filename.substring(0, underscoreIndex);
            String userId = filename.substring(underscoreIndex + 1, dotIndex);
            return new String[] { submissionId, userId };
        }
        return new String[] { "unknown", "unknown" };
    }

}
