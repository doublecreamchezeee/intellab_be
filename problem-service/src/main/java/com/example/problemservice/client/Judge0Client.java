package com.example.problemservice.client;

import com.example.problemservice.model.ProblemSubmission;
import com.example.problemservice.model.TestCase;
import com.example.problemservice.model.TestCase_Output;
import com.example.problemservice.model.composite.testCaseOutputId;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Component
public class Judge0Client {
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Value("${judge0.api.url}")
    private String JUDGE0_BASE_URL;

    // Map to hold language name to language_id mappings
    private static final Map<String, Integer> languageIdMap = new HashMap<>();

    static {
        languageIdMap.put("Assembly (NASM 2.14.02)", 45);
        languageIdMap.put("Bash (5.0.0)", 46);
        languageIdMap.put("Basic (FBC 1.07.1)", 47);
        languageIdMap.put("C (GCC 7.4.0)", 48);
        languageIdMap.put("C++ (GCC 7.4.0)", 52);
        languageIdMap.put("C (GCC 8.3.0)", 49);
        languageIdMap.put("C++ (GCC 8.3.0)", 53);
        languageIdMap.put("C (GCC 9.2.0)", 50);
        languageIdMap.put("C++ (GCC 9.2.0)", 54);
        languageIdMap.put("C# (Mono 6.6.0.161)", 51);
        languageIdMap.put("Common Lisp (SBCL 2.0.0)", 55);
        languageIdMap.put("D (DMD 2.089.1)", 56);
        languageIdMap.put("Elixir (1.9.4)", 57);
        languageIdMap.put("Erlang (OTP 22.2)", 58);
        languageIdMap.put("Fortran (GFortran 9.2.0)", 59);
        languageIdMap.put("Go (1.13.5)", 60);
        languageIdMap.put("Haskell (GHC 8.8.1)", 61);
        languageIdMap.put("Java (OpenJDK 13.0.1)", 62);
        languageIdMap.put("JavaScript (Node.js 12.14.0)", 63);
        languageIdMap.put("Lua (5.3.5)", 64);
        languageIdMap.put("OCaml (4.09.0)", 65);
        languageIdMap.put("Octave (5.1.0)", 66);
        languageIdMap.put("Pascal (FPC 3.0.4)", 67);
        languageIdMap.put("PHP (7.4.1)", 68);
        languageIdMap.put("Plain Text", 43);
        languageIdMap.put("Prolog (GNU Prolog 1.4.5)", 69);
        languageIdMap.put("Python (2.7.17)", 70);
        languageIdMap.put("Python (3.8.1)", 71);
        languageIdMap.put("Ruby (2.7.0)", 72);
        languageIdMap.put("Rust (1.40.0)", 73);
        languageIdMap.put("TypeScript (3.7.4)", 74);
    }

    public Judge0Client(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    // Submit code to Judge0 API and retrieve result
// Submit code to Judge0 API and retrieve result
    public TestCase_Output submitCode(ProblemSubmission submission, TestCase testCase) {
        // Extract details from the submission
        String code = submission.getCode();
        String language = submission.getProgramming_language();

        // Map the programming language to the corresponding language_id
        Integer languageId = languageIdMap.get(language);
        if (languageId == null) {
            throw new IllegalArgumentException("Invalid programming language: " + language);
        }

        // Extract input and expected output from the TestCase
        String input = testCase.getInput();
        String expectedOutput = testCase.getOutput();

        // Create the request body as a Map
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("source_code", code);
        requestBody.put("language_id", languageId);
        requestBody.put("stdin", input);
        requestBody.put("expected_output", expectedOutput);

        // Convert the request body to JSON
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonRequestBody;
        try {
            jsonRequestBody = objectMapper.writeValueAsString(requestBody);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize request body", e);
        }

        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> requestEntity = new HttpEntity<>(jsonRequestBody, headers);

        // Send POST request to Judge0 for code execution
        ResponseEntity<String> response = restTemplate.exchange(
                JUDGE0_BASE_URL + "/submissions", HttpMethod.POST, requestEntity, String.class
        );

        if (response.getStatusCode() == HttpStatus.CREATED) {
            // Extract submission ID from the response body
            String submissionId = extractSubmissionId(Objects.requireNonNull(response.getBody()));

            // Call the status endpoint to get execution result
            TestCase_Output result = getJudge0Result(submissionId, testCase);
            result.setToken(UUID.fromString(submissionId));
            return result;
        } else {
            // Throw a meaningful error with status code and response body
            String errorMessage = String.format(
                    "Failed to submit code to Judge0. Status: %s, Body: %s",
                    response.getStatusCode(),
                    response.getBody()
            );
            throw new RuntimeException(errorMessage);
        }
    }

    // Retrieve Judge0 result using submission ID
    private TestCase_Output getJudge0Result(String submissionId, TestCase testCase) {
        ResponseEntity<String> response = restTemplate.exchange(
                JUDGE0_BASE_URL + "/submissions/" + submissionId, HttpMethod.GET, null, String.class
        );

        if (response.getStatusCode() == HttpStatus.OK) {
            // Parse the result from the response body
            return parseJudge0Result(response.getBody(), testCase);
        } else {
            throw new RuntimeException("Failed to fetch result from Judge0");
        }
    }

    // Retrieve Judge0 result and update TestCase_Output
    public TestCase_Output getSubmissionResult(TestCase_Output testCaseOutput) {
        String submissionId = String.valueOf(testCaseOutput.getToken());

        // Send GET request to Judge0 to retrieve result
        ResponseEntity<String> response = restTemplate.exchange(
                JUDGE0_BASE_URL + "/submissions/" + submissionId, HttpMethod.GET, null, String.class
        );

        if (response.getStatusCode() == HttpStatus.OK) {
            // Parse response body
            try {
                JsonNode rootNode = objectMapper.readTree(response.getBody());

                // Check the status of the result
                String resultStatus = rootNode.path("status").path("description").asText();

                // If still in queue, skip updates
                if ("In Queue".equalsIgnoreCase(resultStatus)) {
                    return testCaseOutput;
                }

                // Update TestCase_Output fields based on the result
                String submissionOutput = rootNode.path("stdout").asText();
                Float runtime = (float) rootNode.path("time").asDouble();

                testCaseOutput.setResult_status(resultStatus);
                testCaseOutput.setSubmission_output(submissionOutput);
                testCaseOutput.setRuntime(runtime);

                return testCaseOutput;
            } catch (Exception e) {
                throw new RuntimeException("Error parsing Judge0 response", e);
            }
        } else {
            throw new RuntimeException("Failed to fetch result from Judge0");
        }
    }

    // Extract the submission ID from the response (based on Judge0 API)
    private String extractSubmissionId(String responseBody) {
        // You can use a JSON parser (e.g., Jackson or Gson) to extract submission_id
        return responseBody.split("\"token\":\"")[1].split("\"")[0];
    }

    // Parse the result from Judge0 response and populate the TestCase_Output object
    public TestCase_Output parseJudge0Result(String responseBody, TestCase testCase) {
        try {
            JsonNode rootNode = objectMapper.readTree(responseBody);

            // Extract the status description from the response
            String resultStatus = rootNode.path("status").path("description").asText();

            // Extract standard output (stdout) from the response
            String submissionOutput = rootNode.path("stdout").asText();

            // Extract runtime (time) from the response
            Float runtime = (float) rootNode.path("time").asDouble();

            // Create and populate the TestCase_Output object
            return TestCase_Output.builder()
                    .runtime(runtime)
                    .submission_output(submissionOutput)
                    .result_status(resultStatus)
                    .testcase(testCase)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Error parsing Judge0 response", e);
        }
    }
}
