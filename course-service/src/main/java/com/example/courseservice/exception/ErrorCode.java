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
    CERTIFICATE_NOT_FOUND(404, "Certificate not found", HttpStatus.NOT_FOUND),
    COURSE_NOT_EXISTED(404, "Course not existed", HttpStatus.NOT_FOUND),
    USER_COURSE_NOT_EXISTED(404, "User course not existed", HttpStatus.NOT_FOUND),
    LESSON_NOT_FOUND(404, "Lesson not found", HttpStatus.NOT_FOUND),
    EXERCISE_NOT_FOUND(404, "Exercise not found", HttpStatus.NOT_FOUND),
    QUESTION_NOT_FOUND(404, "Question not found", HttpStatus.NOT_FOUND),
    OPTION_NOT_FOUND(404, "Option not found", HttpStatus.NOT_FOUND),
    ASSIGNMENT_NOT_FOUND(404, "Assignment not found", HttpStatus.NOT_FOUND),
    USER_NOT_EXISTED_IN_COURSE(404, "User dont existed in course", HttpStatus.NOT_FOUND),
    USER_EXISTED_IN_COURSE(400, "User existed in course", HttpStatus.BAD_REQUEST),
    LESSON_ALREADY_HAD_EXERCISE(400, "Lesson already had exercise", HttpStatus.BAD_REQUEST),
    BAD_REQUEST(400, "Bad request", HttpStatus.BAD_REQUEST),
    LESSON_ORDER_EXISTED(409, "Lesson order existed", HttpStatus.CONFLICT),
    LEARNING_LESSON_EXISTED(409, "Learning lesson existed", HttpStatus.CONFLICT),
    USER_NOT_ENROLLED(404, "User not enrolled", HttpStatus.NOT_FOUND),
    LEARNING_LESSON_NOT_FOUND(404, "Learning lesson not found", HttpStatus.NOT_FOUND),
    REVIEW_NOT_FOUND(404, "Review not found", HttpStatus.NOT_FOUND),
    CATEGORY_NOT_FOUND(404, "Category not found", HttpStatus.NOT_FOUND),
    COMMENT_NOT_FOUND(404, "Comment not found", HttpStatus.NOT_FOUND),
    ;
    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    private int code;
    private String message;
    private HttpStatusCode statusCode;

}
