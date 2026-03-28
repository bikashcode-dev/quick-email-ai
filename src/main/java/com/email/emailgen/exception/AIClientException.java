package com.email.emailgen.exception;

import lombok.Getter;

@Getter
public class AIClientException extends RuntimeException {

    private final String provider;
    private final boolean retryable;

    public AIClientException(String provider, String message, boolean retryable, Throwable cause) {
        super(message, cause);
        this.provider = provider;
        this.retryable = retryable;
    }

}
