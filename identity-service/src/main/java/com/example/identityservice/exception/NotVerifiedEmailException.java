package com.example.identityservice.exception;

public class NotVerifiedEmailException extends AppException {

    private static final long serialVersionUID = 7244519491059365888L;

    private static final String DEFAULT_MESSAGE = "Email is not verified";

    public NotVerifiedEmailException() {
        super(ErrorCode.NOT_VERIFIED_EMAIL);
    }
}
