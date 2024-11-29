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
    ;
    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    private int code;
    private String message;
    private HttpStatusCode statusCode;

}
