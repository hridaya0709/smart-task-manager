package com.ai_practice.github.smart_task_manager.exception;

// create a custom exception class that will be thrown when a resource is not found
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}