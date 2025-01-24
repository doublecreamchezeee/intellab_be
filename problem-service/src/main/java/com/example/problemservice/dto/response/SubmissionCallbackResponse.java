package com.example.problemservice.dto.response;

import lombok.Data;

@Data
public class SubmissionCallbackResponse {
    private String stdout;  // Base64 encoded string
    private String time;    // Time taken
    private int memory;     // Memory usage
    private String stderr;  // Standard error (nullable)
    private String token;   // Token for identifying the submission
    private String message; // Any additional messages
    private Status status;  // Execution status
    private String compile_output;
    @Data
    public static class Status {
        private int id;             // Status ID (e.g., 3)
        private String description; // Status description (e.g., "Accepted")
    }
}
