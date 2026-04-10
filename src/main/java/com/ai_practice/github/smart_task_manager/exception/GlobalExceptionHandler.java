package com.ai_practice.github.smart_task_manager.exception;

import org.apache.coyote.BadRequestException;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.SocketTimeoutException;
import java.net.http.HttpTimeoutException;
import java.util.concurrent.TimeoutException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage()
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequestException(BadRequestException ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AiServiceTimeoutException.class)
    public ResponseEntity<ErrorResponse> handleAiServiceTimeout(AiServiceTimeoutException ex) {
        if (isTimeout(ex)) {
            ErrorResponse error = new ErrorResponse(
                    HttpStatus.GATEWAY_TIMEOUT.value(),
                    ex.getMessage()
            );
            return new ResponseEntity<>(error, HttpStatus.GATEWAY_TIMEOUT);
        }

        // If this exception was raised incorrectly for non-timeout causes.
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_GATEWAY.value(),
                "AI service call failed: " + getRootMessage(ex)
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_GATEWAY);
    }

    private boolean isTimeout(Throwable ex) {
        Throwable root = NestedExceptionUtils.getMostSpecificCause(ex);
        return root instanceof SocketTimeoutException
                || root instanceof HttpTimeoutException
                || root instanceof TimeoutException;
    }

    private String getRootMessage(Throwable ex) {
        Throwable root = NestedExceptionUtils.getMostSpecificCause(ex);
        return (root != null && root.getMessage() != null) ? root.getMessage() : ex.getMessage();
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
         ErrorResponse error = new ErrorResponse(
                 HttpStatus.INTERNAL_SERVER_ERROR.value(),
                 "An unexpected error occurred: " + ex.getMessage()
         );
         return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
     }
}
