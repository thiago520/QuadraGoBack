package com.quadrago.backend.config;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.ProblemDetail;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /* ===== Helpers ===== */

    private ErrorBody body(HttpServletRequest req, HttpStatus status, String message, Object details) {
        return new ErrorBody(Instant.now(), status.value(), status.getReasonPhrase(), message, req.getRequestURI(), details);
    }

    private ResponseEntity<ErrorBody> respond(HttpServletRequest req, HttpStatus status, String message, Object details) {
        return ResponseEntity.status(status).body(body(req, status, message, details));
    }

    /* ===== 400: Bad Request ===== */

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorBody> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest req) {
        log.warn("400 BadRequest: {}", ex.getMessage());
        return respond(req, HttpStatus.BAD_REQUEST, ex.getMessage(), null);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorBody> handleNotReadable(HttpMessageNotReadableException ex, HttpServletRequest req) {
        log.warn("400 NotReadable: {}", ex.getMessage());
        return respond(req, HttpStatus.BAD_REQUEST, "Payload inválido ou malformado.", null);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorBody> handleMissingParam(MissingServletRequestParameterException ex, HttpServletRequest req) {
        log.warn("400 MissingParam: {}", ex.getMessage());
        return respond(req, HttpStatus.BAD_REQUEST, "Parâmetro obrigatório ausente: " + ex.getParameterName(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorBody> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<FieldViolation> fields = ex.getBindingResult().getFieldErrors().stream()
                .map(this::mapFieldError)
                .collect(Collectors.toList());
        String message = "Validação falhou para um ou mais campos.";
        log.warn("400 ValidationError: {} -> {}", message, fields);
        return respond(req, HttpStatus.BAD_REQUEST, message, Map.of("fields", fields));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorBody> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest req) {
        List<PathViolation> violations = ex.getConstraintViolations().stream()
                .map(this::mapConstraint)
                .collect(Collectors.toList());
        String message = "Violação de restrições de validação.";
        log.warn("400 ConstraintViolation: {} -> {}", message, violations);
        return respond(req, HttpStatus.BAD_REQUEST, message, Map.of("violations", violations));
    }

    /* ===== 401: Unauthorized ===== */

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorBody> handleAuth(AuthenticationException ex, HttpServletRequest req) {
        log.warn("401 Unauthorized: {}", ex.getMessage());
        return respond(req, HttpStatus.UNAUTHORIZED, "Não autenticado.", null);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ErrorBody> handleExpiredJwt(ExpiredJwtException ex, HttpServletRequest req) {
        log.warn("401 Token expirado: {}", ex.getMessage());
        return respond(req, HttpStatus.UNAUTHORIZED, "Token expirado.", Map.of("code", "token_expired"));
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ErrorBody> handleJwt(JwtException ex, HttpServletRequest req) {
        log.warn("401 Token inválido: {}", ex.getMessage());
        return respond(req, HttpStatus.UNAUTHORIZED, "Token inválido.", Map.of("code", "token_invalid"));
    }

    /* ===== 403: Forbidden ===== */

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorBody> handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        log.warn("403 Forbidden: {}", ex.getMessage());
        return respond(req, HttpStatus.FORBIDDEN, "Acesso negado.", null);
    }

    /* ===== 404: Not Found ===== */

    @ExceptionHandler({UsernameNotFoundException.class, NoSuchElementException.class})
    public ResponseEntity<ErrorBody> handleNotFound(RuntimeException ex, HttpServletRequest req) {
        log.warn("404 NotFound: {}", ex.getMessage());
        return respond(req, HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    /* ===== 409: Conflict (constraints de banco, unique, FK, etc.) ===== */

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorBody> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest req) {
        log.warn("409 Conflict (integridade BD): {}", ex.getMostSpecificCause().getMessage());
        return respond(req, HttpStatus.CONFLICT, "Violação de integridade de dados.", null);
    }

    /* ===== Propaga status vindo de ErrorResponseException (Spring 6/Boot 3) ===== */
    @ExceptionHandler(ErrorResponseException.class)
    public ResponseEntity<ErrorBody> handleErrorResponse(ErrorResponseException ex, HttpServletRequest req) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        ProblemDetail pd = ex.getBody(); // pode ser null
        String title = pd != null && pd.getTitle() != null ? pd.getTitle() : status.getReasonPhrase();
        String detail = pd != null && pd.getDetail() != null ? pd.getDetail() : ex.getMessage();

        return ResponseEntity.status(status)
                .body(body(req, status, detail, Map.of("title", title)));
    }

    /* ===== Compatibilidade: ResponseStatusException (tem getReason()) ===== */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorBody> handleResponseStatus(ResponseStatusException ex, HttpServletRequest req) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        String reason = ex.getReason() != null ? ex.getReason() : status.getReasonPhrase();
        return ResponseEntity.status(status).body(body(req, status, reason, null));
    }

    /* ===== 500: Fallback ===== */

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorBody> handleGeneric(Exception ex, HttpServletRequest req) {
        log.error("500 InternalError: {}", ex.getMessage(), ex);
        return respond(req, HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno. Tente novamente mais tarde.", null);
    }

    /* ===== DTOs ===== */

    public record ErrorBody(
            Instant timestamp,
            int status,
            String error,
            String message,
            String path,
            Object details
    ) {
    }

    public record FieldViolation(
            String field,
            String message,
            Object rejectedValue
    ) {
    }

    public record PathViolation(
            String path,
            String message,
            Object invalidValue
    ) {
    }

    private FieldViolation mapFieldError(FieldError e) {
        Object rejected = e.isBindingFailure() ? null : e.getRejectedValue();
        return new FieldViolation(e.getField(), Optional.ofNullable(e.getDefaultMessage()).orElse("inválido"), rejected);
    }

    private PathViolation mapConstraint(ConstraintViolation<?> v) {
        return new PathViolation(
                String.valueOf(v.getPropertyPath()),
                Optional.ofNullable(v.getMessage()).orElse("inválido"),
                v.getInvalidValue()
        );
    }
}
