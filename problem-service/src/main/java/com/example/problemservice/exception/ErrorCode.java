package com.example.problemservice.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(600,"Uncategorized exception", HttpStatus.INTERNAL_SERVER_ERROR),
    PROBLEM_NOT_EXIST(404,"Problem not exist", HttpStatus.NOT_FOUND),
    SUBMISSION_NOT_EXIST(404,"Submission not exist", HttpStatus.NOT_FOUND),
    SOLUTION_NOT_EXIST(404,"Solution not exist", HttpStatus.NOT_FOUND),
    TESTCASE_NOT_EXIST(404,"Test case not exist", HttpStatus.NOT_FOUND),
    INVALID_PROGRAMMING_LANGUAGE(404,"Invalid programming language", HttpStatus.BAD_REQUEST),
    PROGRAMMING_LANGUAGE_NOT_EXIST(404,"Programming language not exist", HttpStatus.NOT_FOUND),
    TEST_CASE_OUTPUT_NOT_EXIST(404,"Test case output not exist", HttpStatus.NOT_FOUND),
    TEST_CASE_RUN_CODE_OUTPUT_NOT_EXIST(404,"Test case run code output not exist", HttpStatus.NOT_FOUND),
    RUN_CODE_NOT_EXISTED(404,"Run code not existed", HttpStatus.NOT_FOUND),
    COMMENT_NOT_EXIST(404,"Comment not exist", HttpStatus.NOT_FOUND),
    PARENT_COMMENT_NOT_EXIST(404,"Parent comment not exist", HttpStatus.NOT_FOUND),
    INVALID_PARENT_COMMENT_ID(400,"Invalid parent comment id", HttpStatus.BAD_REQUEST),
    PROBLEM_ID_NOT_SAME_AS_PROBLEM_ID_OF_PARENT_COMMENT(400,"Problem id in request not same as problem id of parent comment", HttpStatus.BAD_REQUEST),
    PROBLEM_ID_NOT_SAME_AS_PROBLEM_ID_OF_REPLIED_COMMENT(400,"Problem id in request not same as problem id of replied comment", HttpStatus.BAD_REQUEST),
    INVALID_REPLIED_COMMENT_ID(400,"Invalid replied comment id", HttpStatus.BAD_REQUEST),
    REPLIED_COMMENT_NOT_EXIST(404,"Replied comment not exist", HttpStatus.NOT_FOUND),
    USER_DONT_HAVE_PERMISSION_TO_UPDATE_COMMENT(403,"User don't have permission to update this comment", HttpStatus.FORBIDDEN),
    USER_DONT_HAVE_PERMISSION_TO_DELETE_COMMENT(403,"User don't have permission to delete this comment", HttpStatus.FORBIDDEN),
    IDENTITY_SERVER_ERROR(500,"Identity server error", HttpStatus.INTERNAL_SERVER_ERROR),
    UNAUTHORIZED(401,"Unauthorized", HttpStatus.UNAUTHORIZED),
    BAD_REQUEST(400,"Bad request", HttpStatus.BAD_REQUEST),
    REACTION_EXISTED(409,"Reaction existed", HttpStatus.CONFLICT),
    REACTION_NOT_EXISTED(404,"Reaction not existed", HttpStatus.NOT_FOUND),
    PROBLEM_NOT_PUBLISHED(403,"Problem not published. Please upgrade your account to access.", HttpStatus.FORBIDDEN),
    EMAIL_NOT_VERIFIED(403,"Email is not verified", HttpStatus.FORBIDDEN),
    PROBLEM_NOT_COMPLETE(409, "Problem is not created completely", HttpStatus.CONFLICT),
    CUSTOM_CHECKER_NOT_ENABLED_FOR_PROBLEM(409, "Custom checker is not enabled for this problem", HttpStatus.CONFLICT),
    STATUS_ID_NOT_FOUND(404, "Status ID not found in the request", HttpStatus.NOT_FOUND),
    INVALID_OOP_METADATA(409, "Invalid OOP metadata", HttpStatus.CONFLICT),

    ;
    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    private int code;
    private String message;
    private HttpStatusCode statusCode;

}
