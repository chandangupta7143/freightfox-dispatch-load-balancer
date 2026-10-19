package com.chandan.freightfox.exception;

import com.chandan.freightfox.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

/**
 * Centralized exception handler for mapping exceptions to clean, secure HTTP responses.
 * Never exposes SQL traces, stack traces, or credentials in client responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles Bean Validation errors on @Valid request bodies.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<String> errors = new ArrayList<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.add(error.getField() + ": " + error.getDefaultMessage());
        }
        ex.getBindingResult().getGlobalErrors().forEach(error -> {
            errors.add(error.getDefaultMessage());
        });

        logger.warn("Validation failed for request: {}", errors);
        ErrorResponse response = new ErrorResponse("error", "Validation failed", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handles business validation exceptions (e.g. duplicate IDs in batch).
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        logger.warn("Invalid argument in request: {}", ex.getMessage());
        ErrorResponse response = new ErrorResponse("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handles malformed JSON payloads.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMalformedJson(HttpMessageNotReadableException ex) {
        logger.warn("Malformed JSON in request: {}", ex.getMessage());
        ErrorResponse response = new ErrorResponse("error", "Malformed JSON request body or invalid data format");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Fallback handler for any unexpected internal server error.
     * Logs the full stack trace on the server, but returns a generic safe message to the client.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        logger.error("Unexpected server error occurred", ex);
        ErrorResponse response = new ErrorResponse("error", "An unexpected error occurred. Please verify your request or try again later.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
