package com.ecoledger.auditoria.application.controller;

import com.ecoledger.auditoria.application.exception.AuditoriaNotFoundException;
import com.ecoledger.auditoria.application.exception.RevisaoInvalidaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.stream.Collectors;

/**
 * Global exception handler using RFC 7807 Problem Details.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AuditoriaNotFoundException.class)
    public ProblemDetail handleAuditoriaNotFound(AuditoriaNotFoundException ex) {
        log.warn("Auditoria not found: {}", ex.getAuditoriaId());
        
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Auditoria Not Found");
        problem.setType(URI.create("https://api.ecoledger.local/errors/auditoria-not-found"));
        problem.setProperty("auditoriaId", ex.getAuditoriaId());
        problem.setProperty("timestamp", Instant.now());
        
        return problem;
    }

    @ExceptionHandler(RevisaoInvalidaException.class)
    public ProblemDetail handleRevisaoInvalida(RevisaoInvalidaException ex) {
        log.warn("Invalid revision: {}", ex.getMessage());
        
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Invalid Revision");
        problem.setType(URI.create("https://api.ecoledger.local/errors/revisao-invalida"));
        problem.setProperty("timestamp", Instant.now());
        
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationErrors(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        
        log.warn("Validation failed: {}", errors);
        
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Validation failed: " + errors);
        problem.setTitle("Validation Error");
        problem.setType(URI.create("https://api.ecoledger.local/errors/validation-error"));
        problem.setProperty("timestamp", Instant.now());
        
        return problem;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Invalid argument: {}", ex.getMessage());
        
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Invalid Argument");
        problem.setType(URI.create("https://api.ecoledger.local/errors/invalid-argument"));
        problem.setProperty("timestamp", Instant.now());
        
        return problem;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        log.error("Unexpected error", ex);
        
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        problem.setTitle("Internal Server Error");
        problem.setType(URI.create("https://api.ecoledger.local/errors/internal-error"));
        problem.setProperty("timestamp", Instant.now());
        
        return problem;
    }
}
