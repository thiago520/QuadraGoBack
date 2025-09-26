package com.quadrago.backend.exceptions;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /* ------------ Validations (DTO/body/params) ------------ */

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationErrors(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::formatFieldError)
                .toList();

        log.warn("400 Bad Request - validation errors: {}", errors);
        return build(HttpStatus.BAD_REQUEST, "Validation failed", Map.of("errors", errors));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException ex) {
        List<String> errors = ex.getConstraintViolations()
                .stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .toList();

        log.warn("400 Bad Request - constraint violations: {}", errors);
        return build(HttpStatus.BAD_REQUEST, "Constraint violation", Map.of("errors", errors));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Object> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = String.format("Parameter '%s' with value '%s' could not be converted to type '%s'",
                ex.getName(), ex.getValue(), ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");
        log.warn("400 Bad Request - {}", message);
        return build(HttpStatus.BAD_REQUEST, message, null);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Object> handleMissingParam(MissingServletRequestParameterException ex) {
        String message = String.format("Missing required parameter '%s'", ex.getParameterName());
        log.warn("400 Bad Request - {}", message);
        return build(HttpStatus.BAD_REQUEST, message, null);
    }

    /* ------------------- AuthZ/AuthN ------------------- */

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Object> handleBadCredentials(BadCredentialsException ex) {
        log.warn("401 Unauthorized - {}", ex.getMessage());
        return build(HttpStatus.UNAUTHORIZED, "Invalid credentials", null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDenied(AccessDeniedException ex) {
        log.warn("403 Forbidden - {}", ex.getMessage());
        return build(HttpStatus.FORBIDDEN, "Access denied", null);
    }

    /* ------------------- Persistence ------------------- */

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Object> handleEntityNotFound(EntityNotFoundException ex) {
        log.warn("404 Not Found - {}", ex.getMessage());
        return build(HttpStatus.NOT_FOUND, ex.getMessage() != null ? ex.getMessage() : "Resource not found", null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("409 Conflict - Data integrity violation: {}", ex.getMostSpecificCause().getMessage());
        return build(HttpStatus.CONFLICT, "Data integrity violation", null);
    }

    /* ------------------- Generic fallbacks ------------------- */

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("400 Bad Request - {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Object> handleNoSuchElement(NoSuchElementException ex) {
        log.warn("404 Not Found - {}", ex.getMessage());
        return build(HttpStatus.NOT_FOUND, ex.getMessage() != null ? ex.getMessage() : "Resource not found", null);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleRuntime(RuntimeException ex) {
        String msg = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName();
        log.error("500 Internal Server Error - {}", msg, ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", Map.of("detail", msg));
    }

    /* ------------------- Helpers ------------------- */

    private String formatFieldError(FieldError error) {
        return String.format("%s: %s", error.getField(), error.getDefaultMessage());
    }

    private ResponseEntity<Object> build(HttpStatus status, String message, Map<String, Object> extra) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);

        if (extra != null && !extra.isEmpty()) {
            body.putAll(extra);
        }

        return new ResponseEntity<>(body, status);
    }
}
