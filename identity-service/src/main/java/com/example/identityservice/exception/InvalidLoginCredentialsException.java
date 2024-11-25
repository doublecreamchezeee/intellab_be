package com.example.identityservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;

import java.io.Serial;

public class InvalidLoginCredentialsException extends ResponseStatusException {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String DEFAULT_MESSAGE = "Invalid login credentials provided";

    public InvalidLoginCredentialsException(String s, HttpClientErrorException exception) {
        super(HttpStatus.UNAUTHORIZED, DEFAULT_MESSAGE);
    }

}