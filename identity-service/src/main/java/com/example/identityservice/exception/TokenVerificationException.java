package com.example.identityservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.io.Serial;

public class TokenVerificationException extends ResponseStatusException {

    @Serial
    private static final long serialVersionUID = -7244519491059365888L;

    private static final String DEFAULT_MESSAGE = "Authentication failure: Token missing, invalid or expired";

    public TokenVerificationException() {
        super(HttpStatus.UNAUTHORIZED, DEFAULT_MESSAGE);
    }

}