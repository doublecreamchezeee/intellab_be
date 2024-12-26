package com.example.identityservice.exception;

import org.springframework.web.client.HttpClientErrorException;

import java.io.Serial;

public class InvalidLoginCredentialsException extends AppException {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String DEFAULT_MESSAGE = "Invalid login credentials provided";

    public InvalidLoginCredentialsException(String s, HttpClientErrorException exception) {
        //super(HttpStatus.UNAUTHORIZED, DEFAULT_MESSAGE);
        super(ErrorCode.INVALID_LOGIN_CREDENTIALS);
    }

    public InvalidLoginCredentialsException(String s, InvalidLoginCredentialsException e) {
        super(ErrorCode.INVALID_LOGIN_CREDENTIALS);
    }
}