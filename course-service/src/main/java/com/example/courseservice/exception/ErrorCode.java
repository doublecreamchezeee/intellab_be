package com.example.courseservice.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(600,"Uncategorized exception", HttpStatus.INTERNAL_SERVER_ERROR),

    USER_EXISTED(200, "User existed", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(404, "User not existed", HttpStatus.NOT_FOUND),
    USERNAME_INVALID(400,"Username is invalid", HttpStatus.BAD_REQUEST),
    PASSWORD_INVALID(400,"Password is invalid", HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED(500,"Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(501,"Dont have permission", HttpStatus.FORBIDDEN),
    COURSE_NOT_EXISTED(404, "Course not existed", HttpStatus.NOT_FOUND),
    LESSON_NOT_FOUND(404, "Lesson not found", HttpStatus.NOT_FOUND),
    BAD_REQUEST(400, "Bad request", HttpStatus.BAD_REQUEST),
    ;
    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    private int code;
    private String message;
    private HttpStatusCode statusCode;

}
