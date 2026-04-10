package com.ai_practice.github.smart_task_manager.exception;

import org.springframework.web.client.RestClientException;

public class AiServiceTimeoutException extends RuntimeException {
    public AiServiceTimeoutException(String message, RestClientException ex) {
        super(message, ex);
    }
}
