package com.example.problemservice.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(600,"Uncategorized exception", HttpStatus.INTERNAL_SERVER_ERROR),
    PROBLEM_NOT_EXIST(404,"Problem not exist", HttpStatus.BAD_REQUEST),
    SUBMISSION_NOT_EXIST(404,"Submission not exist", HttpStatus.BAD_REQUEST),
    SOLUTION_NOT_EXIST(404,"Solution not exist", HttpStatus.BAD_REQUEST),
    TESTCASE_NOT_EXIST(404,"Test case not exist", HttpStatus.BAD_REQUEST),
    INVALID_PROGRAMMING_LANGUAGE(404,"Invalid programming language", HttpStatus.BAD_REQUEST),
    PROGRAMMING_LANGUAGE_NOT_EXIST(404,"Programming language not exist", HttpStatus.BAD_REQUEST),
    ;
    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    private int code;
    private String message;
    private HttpStatusCode statusCode;

}
