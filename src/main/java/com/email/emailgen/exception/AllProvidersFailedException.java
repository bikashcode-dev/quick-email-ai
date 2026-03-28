package com.email.emailgen.exception;

public class AllProvidersFailedException extends RuntimeException {
    public AllProvidersFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
