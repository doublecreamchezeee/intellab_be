package com.example.problemservice.client;

import com.example.problemservice.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Component
@Slf4j
public class Judge0Client {
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Value("${judge0.api.url}")
    private String JUDGE0_BASE_URL;

    @Value("${judge0.api.callback_url}")
    private String CALLBACK_BASE_URL;

    @Value("${judge0.api.run_code_callback_url}")
    private String RUN_CODE_CALLBACK_BASE_URL;
    // Map to hold language name to language_id mappings
    private static final Map<String, Integer> languageIdMap = new HashMap<>();

    static {
        languageIdMap.put("C (GCC 7.4.0)", 48);
        languageIdMap.put("C++ (GCC 7.4.0)", 52);
        languageIdMap.put("C (GCC 8.3.0)", 49);
        languageIdMap.put("C++ (GCC 8.3.0)", 53);
        languageIdMap.put("C (GCC 9.2.0)", 50);
        languageIdMap.put("C++ (GCC 9.2.0)", 54);
        languageIdMap.put("C# (Mono 6.6.0.161)", 51);
        languageIdMap.put("Java (JDK 17.0.6)", 62);
        languageIdMap.put("JavaScript (Node.js 12.14.0)", 63);
        languageIdMap.put("Python (3.8.1)", 71);
        languageIdMap.put("TypeScript (3.7.4)", 74);
    }

    public Judge0Client(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    // Submit code to Judge0 API and retrieve result
// Submit code to Judge0 API and retrieve result
    public TestCaseOutput submitCode(ProblemSubmission submission, TestCase testCase) {
        // Extract details from the submission
        String code = submission.getCode();
        String language = submission.getProgrammingLanguage();

        // Map the programming language to the corresponding language_id
        Integer languageId = languageIdMap.get(language);
        if (languageId == null) {
            throw new IllegalArgumentException("Invalid programming language: " + language);
        }
//        String boilerplateCode = boilerplateClient.enrich(code, languageId);

        // Extract input and expected output from the TestCase
        String input = testCase.getInput();
        String expectedOutput = testCase.getOutput();

        // Create the request body as a Map
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("source_code", code);
        requestBody.put("language_id", languageId);
        requestBody.put("stdin", input);
        requestBody.put("expected_output", expectedOutput);
        requestBody.put("callback_url", CALLBACK_BASE_URL);
        // Convert the request body to JSON
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonRequestBody;
        try {
            jsonRequestBody = objectMapper.writeValueAsString(requestBody);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize request body", e);
        }

        log.info("Request body: {}", jsonRequestBody);
        //return null;

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
            TestCaseOutput result = getJudge0Result(submissionId, testCase);
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
    private TestCaseOutput getJudge0Result(String submissionId, TestCase testCase) {
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

    // Retrieve Judge0 result and update TestCaseOutput
    public TestCaseOutput getSubmissionResult(TestCaseOutput testCaseOutput) {
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

                // Update TestCaseOutput fields based on the result
                String submissionOutput = rootNode.path("stdout").asText();
                Float runtime = (float) rootNode.path("time").asDouble();
                Float memory = (float) rootNode.path("memory").asDouble();
                testCaseOutput.setResult_status(resultStatus);
                testCaseOutput.setSubmission_output(submissionOutput);
                testCaseOutput.setRuntime(runtime);
                testCaseOutput.setMemory(memory);
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

    // Parse the result from Judge0 response and populate the TestCaseOutput object
    public TestCaseOutput parseJudge0Result(String responseBody, TestCase testCase) {
        try {
            JsonNode rootNode = objectMapper.readTree(responseBody);

            // Extract the status description from the response
            String resultStatus = rootNode.path("status").path("description").asText();

            // Extract standard output (stdout) from the response
            String submissionOutput = rootNode.path("stdout").asText();

            // Extract runtime (time) from the response
            Float runtime = (float) rootNode.path("time").asDouble();

            Float memory = (float) rootNode.path("memory").asDouble();

            // Create and populate the TestCaseOutput object
            return TestCaseOutput.builder()
                    .runtime(runtime)
                    .memory(memory)
                    .submission_output(submissionOutput)
                    .result_status(resultStatus)
                    .testcase(testCase)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Error parsing Judge0 response", e);
        }
    }

    public TestCaseRunCodeOutput runCode(ProblemRunCode problemRunCode, TestCase testCase) {
        // Extract details from the submission
        String code = problemRunCode.getCode();
        String language = problemRunCode.getProgrammingLanguage();

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
        requestBody.put("callback_url", RUN_CODE_CALLBACK_BASE_URL);

        // Convert the request body to JSON
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonRequestBody;
        try {
            jsonRequestBody = objectMapper.writeValueAsString(requestBody);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize request body", e);
        }

        log.info("Request body: {}", jsonRequestBody);

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
            TestCaseRunCodeOutput result = getJudge0RunCodeResult(submissionId, testCase);
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

    private TestCaseRunCodeOutput getJudge0RunCodeResult(String runCodeId, TestCase testCase) {
        ResponseEntity<String> response = restTemplate.exchange(
                JUDGE0_BASE_URL + "/submissions/" + runCodeId, HttpMethod.GET, null, String.class
        );

        if (response.getStatusCode() == HttpStatus.OK) {
            // Parse the result from the response body
            return parseJudge0RunCodeResult(response.getBody(), testCase);
        } else {
            throw new RuntimeException("Failed to fetch run code result from Judge0");
        }
    }

    private TestCaseRunCodeOutput parseJudge0RunCodeResult(String responseBody, TestCase testCase) {
        try {
            JsonNode rootNode = objectMapper.readTree(responseBody);

            // Extract the status description from the response
            String resultStatus = rootNode.path("status").path("description").asText();

            // Extract standard output (stdout) from the response
            String submissionOutput = rootNode.path("stdout").asText();

            // Extract runtime (time) from the response
            Float runtime = (float) rootNode.path("time").asDouble();

            // Create and populate the TestCaseOutput object
            return TestCaseRunCodeOutput.builder()
                    .runtime(runtime)
                    .submissionOutput(submissionOutput)
                    .resultStatus(resultStatus)
                    .testcase(testCase)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Error parsing Judge0 response", e);
        }
    }
}
