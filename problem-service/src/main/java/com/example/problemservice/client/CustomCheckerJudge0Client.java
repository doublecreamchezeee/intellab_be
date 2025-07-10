package com.example.problemservice.client;

import com.example.problemservice.core.TokenExtractor;
import com.example.problemservice.dto.request.judge0.TestCaseRequest;
import com.example.problemservice.dto.request.judge0.customChecker.SubmissionToGetUserCodeActualOutputRequest;
import com.example.problemservice.model.Problem;
import com.example.problemservice.model.ProblemRunCode;
import com.example.problemservice.model.TestCase;
import com.example.problemservice.model.TestCaseRunCodeOutput;
import com.example.problemservice.repository.TestCaseRunCodeOutputRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Component
@Slf4j
@Data
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class CustomCheckerJudge0Client {
    final ObjectMapper objectMapper;
    final RestTemplate restTemplate;
    final TestCaseRunCodeOutputRepository testCaseRunCodeOutputRepository;
    final CustomCheckerBoilerplateClient customCheckerBoilerplateClient;
    final BoilerplateClient boilerplateClient;

    @Value("${judge0.api.url}")
    String JUDGE0_BASE_URL;

    @Value("${judge0.api.custom_checker_callback_url_to_get_actual_output}")
    String CALLBACK_BASE_URL_TO_GET_ACTUAL_OUTPUT;

    @Value("${judge0.api.custom_checker_run_code_callback_url_to_get_actual_output}")
    String RUN_CODE_CALLBACK_BASE_URL_TO_GET_ACTUAL_OUTPUT;

    @Value("${judge0.api.custom_checker_run_code_callback_url_to_get_checking_result}")
    String RUN_CODE_CALLBACK_BASE_URL_TO_GET_CHECKING_RESULT;

    static final Map<String, Integer> languageIdMap = new HashMap<>();

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

    // Extract the submission ID from the response (based on Judge0 API)
    private String extractSubmissionId(String responseBody) {
        // You can use a JSON parser (e.g., Jackson or Gson) to extract submission_id
        return responseBody.split("\"token\":\"")[1].split("\"")[0];
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

        log.info("Request body: {}", code);

        // Create the request body as a Map
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("source_code", code);
        requestBody.put("language_id", languageId);
        requestBody.put("stdin", input);
        requestBody.put("expected_output", expectedOutput);
        requestBody.put("callback_url", RUN_CODE_CALLBACK_BASE_URL_TO_GET_ACTUAL_OUTPUT);

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

    public List<TestCaseRunCodeOutput> runCodeBatchToGetActualOutput(ProblemRunCode problemRunCode, List<TestCase> testCases) {
        // Extract details from the submission
        String code = problemRunCode.getCode();
        String language = problemRunCode.getProgrammingLanguage();

        // Map the programming language to the corresponding language_id
        Integer languageId = languageIdMap.get(language);
        if (languageId == null) {
            throw new IllegalArgumentException("Invalid programming language: " + language);
        }

        List<SubmissionToGetUserCodeActualOutputRequest> submissions = new ArrayList<>();
        for (TestCase testCase : testCases) {
            // Extract input and expected output from the TestCase
            String input = testCase.getInput();
            //String expectedOutput = testCase.getOutput();

            SubmissionToGetUserCodeActualOutputRequest request = SubmissionToGetUserCodeActualOutputRequest
                    .builder()
                    .source_code(code)
                    .language_id(languageId)
                    .stdin(input)
                    .callback_url(RUN_CODE_CALLBACK_BASE_URL_TO_GET_ACTUAL_OUTPUT)
                    .build();

            submissions.add(request);
        }

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("submissions", submissions);

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
                JUDGE0_BASE_URL + "/submissions/batch", HttpMethod.POST, requestEntity, String.class
        );

        List<TestCaseRunCodeOutput> results = new ArrayList<>();

        if (response.getStatusCode() == HttpStatus.CREATED) {
            // Extract submission ID from the response body
            List<String> listTokens= TokenExtractor.extractTokens(Objects.requireNonNull(response.getBody()));

            for (int i = 0; i < listTokens.size(); i++) {
                String submissionId = listTokens.get(i);
                // Call the status endpoint to get execution result
                TestCaseRunCodeOutput result = TestCaseRunCodeOutput
                        .builder()
                        .resultStatus("In Queue")
                        .token(UUID.fromString(submissionId))
                        .build();
                results.add(result);
            }

            return results;
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

    public void runCodeToCheckOneTestCaseByCustomChecker(TestCaseRunCodeOutput output, TestCase testCase, Problem problem) {
        // parse the problem structure
        /*BoilerplateClient.BoilerPlateGenerator boilerPlateGenerator = new BoilerplateClient.BoilerPlateGenerator();
        boilerPlateGenerator.parse(problem.getProblemStructure());

        // Enrich the custom checker code with boilerplate
        String code = customCheckerBoilerplateClient.enrichCode(
                boilerPlateGenerator,
                problem.getCustomCheckerCode()
        );

        log.info("Enriched code with custom checker (not used): {}", code);*/

        TestCaseRequest testCaseRequest = TestCaseRequest
                .builder()
                //.source_code(problem.getCustomCheckerCode()) // code
               // .language_id(problem.getCustomCheckerLanguageId())
                .stdin(output.getSubmissionOutput()+ "\n" + testCase.getOutput()) // Concatenate actual output and expected output
                .expected_output("true")
                .callback_url(RUN_CODE_CALLBACK_BASE_URL_TO_GET_CHECKING_RESULT)
                .build();

        // Convert the request body to JSON
        String jsonRequestBody;
        try {
            jsonRequestBody = objectMapper.writeValueAsString(testCaseRequest);
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
            output.setToken(UUID.fromString(submissionId));

            testCaseRunCodeOutputRepository.save(output); // overwrite the existing output's token
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
}
