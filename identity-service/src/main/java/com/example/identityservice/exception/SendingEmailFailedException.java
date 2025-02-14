package com.example.identityservice.exception;

public class SendingEmailFailedException extends AppException{

        private static final long serialVersionUID = 7244519491059365890L;

        private static final String DEFAULT_MESSAGE = "Sending email failed";

        public SendingEmailFailedException() {
            super(ErrorCode.SENDING_EMAIL_FAILED);
        }
}
