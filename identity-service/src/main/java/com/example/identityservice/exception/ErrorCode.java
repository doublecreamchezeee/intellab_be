package com.example.identityservice.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(600,"Uncategorized exception", HttpStatus.INTERNAL_SERVER_ERROR),

    USER_EXISTED(409, "User existed", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(404, "User not existed", HttpStatus.NOT_FOUND),
    USERNAME_INVALID(400,"Username is invalid", HttpStatus.BAD_REQUEST),
    PASSWORD_INVALID(400,"Password is invalid", HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED(401,"Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(401,"Dont have permission", HttpStatus.FORBIDDEN),
    INTERNAL_SERVER_ERROR(500, "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),
    BAD_REQUEST(400, "Bad request", HttpStatus.BAD_REQUEST),
    INVALID_LOGIN_CREDENTIALS(401, "Invalid login credentials", HttpStatus.UNAUTHORIZED),
    TOKEN_VERIFICATION_FAILURE(401, "Authentication failure: Token missing, invalid or expired", HttpStatus.UNAUTHORIZED),
    ACCOUNT_ALREADY_EXISTS(409, "Account already exists", HttpStatus.CONFLICT),
    FIREBASE_AUTHENTICATION_FAILURE(401, "Firebase Authentication error", HttpStatus.UNAUTHORIZED),
    SENDING_EMAIL_FAILED(500, "Sending email failed", HttpStatus.INTERNAL_SERVER_ERROR),
    NOT_VERIFIED_EMAIL(401, "Email is not verified", HttpStatus.UNAUTHORIZED),
    COURSE_NOT_EXISTED(404, "Course not existed", HttpStatus.NOT_FOUND),
    CANNOT_CREATE_PAYMENT(500, "Cannot create payment", HttpStatus.INTERNAL_SERVER_ERROR),
    CANNOT_HANDLE_IPN_CALLBACK(500, "Cannot handle IPN callback", HttpStatus.INTERNAL_SERVER_ERROR),
    PAYMENT_NOT_FOUND(404, "Payment not found", HttpStatus.NOT_FOUND),
    CANNOT_ENROLL_COURSE(500, "Cannot enroll course! Course service error", HttpStatus.INTERNAL_SERVER_ERROR),
    PAYMENT_NOT_SUCCESSFUL(500, "Payment not successful", HttpStatus.INTERNAL_SERVER_ERROR),
    USER_ALREADY_HAS_SUBSCRIPTION(409, "User already has subscription", HttpStatus.CONFLICT),
    ;
    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    private int code;
    private String message;
    private HttpStatusCode statusCode;

}
