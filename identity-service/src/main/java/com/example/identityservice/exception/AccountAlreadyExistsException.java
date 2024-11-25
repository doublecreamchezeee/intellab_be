package com.example.identityservice.exception;

import lombok.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.io.Serial;

public class AccountAlreadyExistsException extends ResponseStatusException {

    @Serial
    private static final long serialVersionUID = 7439642984069939024L;

    public AccountAlreadyExistsException(@NonNull final String reason) {
        super(HttpStatus.CONFLICT, reason);
    }

}