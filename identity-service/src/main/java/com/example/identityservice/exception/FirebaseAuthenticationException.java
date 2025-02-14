package com.example.identityservice.exception;

public class FirebaseAuthenticationException  extends AppException {

    private static final long serialVersionUID = 7244519491059365889L;

    private static final String DEFAULT_MESSAGE = "Authentication failure: Token missing, invalid or expired";

    public FirebaseAuthenticationException() {
        super(ErrorCode.FIREBASE_AUTHENTICATION_FAILURE);
    }
}
