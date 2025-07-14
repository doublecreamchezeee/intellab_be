package com.example.identityservice.exception;

import com.example.identityservice.dto.ApiResponse;
import com.google.firebase.auth.FirebaseAuthException;
import kotlin.io.AccessDeniedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.error("Validation error: {}", ex.getMessage());
        /*Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );*/

        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .reduce("", (acc, err) -> acc + err + "\n");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiResponse.builder()
                        .code(ErrorCode.BAD_REQUEST.getCode())
                        .message(errors)
                        .build()
        );
    }

    @ExceptionHandler(FirebaseAuthException.class)
    public ResponseEntity<ApiResponse> handleFirebaseAuthException(FirebaseAuthException ex) {
        log.error("Firebase Authentication error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ApiResponse.builder()
                        .code(ErrorCode.UNAUTHORIZED.getCode())
                        .message(ex.getMessage())
                        .build()
        );
    }

    @ExceptionHandler(InvalidLoginCredentialsException.class)
    public ResponseEntity<ApiResponse> handleInvalidLoginCredentialsException(InvalidLoginCredentialsException ex) {
        log.error("Invalid login credentials provided: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ApiResponse.builder()
                        .code(ErrorCode.UNAUTHORIZED.getCode())
                        .message(ex.getMessage())
                        .build()
        );
    }

    @ExceptionHandler(AccountAlreadyExistsException.class)
    public ResponseEntity<ApiResponse> handleAccountAlreadyExistsException(AccountAlreadyExistsException ex) {
        log.error("Account already exists: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ApiResponse.builder()
                        .code(ErrorCode.USER_EXISTED.getCode())
                        .message(ex.getMessage())
                        .build()
        );
    }

    @ExceptionHandler(TokenVerificationException.class)
    public ResponseEntity<ApiResponse> handleTokenVerificationException(TokenVerificationException ex) {
        log.error("Authentication failure: Token missing, invalid or expired: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ApiResponse.builder()
                        .code(ErrorCode.UNAUTHORIZED.getCode())
                        .message(ex.getMessage())
                        .build()
        );
    }

    @ExceptionHandler(NotVerifiedEmailException.class)
    public ResponseEntity<ApiResponse> handleNotVerifiedEmailException(NotVerifiedEmailException ex) {
        log.error("Email is not verified: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ApiResponse.builder()
                        .code(ErrorCode.NOT_VERIFIED_EMAIL.getCode())
                        .message(ex.getMessage())
                        .build()
        );
    }

    @ExceptionHandler(FirebaseAuthenticationException.class)
    public ResponseEntity<ApiResponse> handleFirebaseAuthenticationException(FirebaseAuthenticationException ex) {
        log.error("Authentication failure: Token missing, invalid or expired: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ApiResponse.builder()
                        .code(ErrorCode.FIREBASE_AUTHENTICATION_FAILURE.getCode())
                        .message(ex.getMessage())
                        .build()
        );
    }

    @ExceptionHandler(value = AccessDeniedException.class)
    ResponseEntity<ApiResponse> handlingAccessDeniedException(AccessDeniedException e) {
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;
        return ResponseEntity.status(errorCode.getStatusCode()).body(
                ApiResponse.builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build()
        );
    }
    @ExceptionHandler(value = IllegalArgumentException.class)
    ResponseEntity<ApiResponse> handlingIllegalArgumentException(IllegalArgumentException e) {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setMessage(e.getMessage());
        apiResponse.setCode(ErrorCode.BAD_REQUEST.getCode());
        return ResponseEntity.badRequest().body(apiResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleGenericExceptions(Exception ex) {
        log.error("An unexpected error occurred: {}", ex.getMessage(), ex);
        if (ex instanceof AppException) {
            AppException appEx = (AppException) ex;
            HttpStatus statusCode = appEx.getErrorCode().getStatusCode();
            //HttpStatus statusCode = appEx.getErrorCode().getCode() >= 500 ? HttpStatus.INTERNAL_SERVER_ERROR : HttpStatus.CLIENT_ERROR;
            return ResponseEntity.status(statusCode).body(
                    ApiResponse.builder()
                            .code(appEx.getErrorCode().getCode())
                            .message(appEx.getMessage())
                            .build()
            );
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.builder()
                        .code(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
                        .message(ex.getMessage())
                        .build()
                );
    }
}