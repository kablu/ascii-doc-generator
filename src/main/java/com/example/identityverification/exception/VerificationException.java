package com.example.identityverification.exception;

public class VerificationException extends RuntimeException {

    private final String errorCode;

    public VerificationException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
