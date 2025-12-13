package com.ecoledger.movimentacao.application.controller;

import com.ecoledger.movimentacao.application.service.InvalidAttachmentException;
import com.ecoledger.movimentacao.application.service.MovimentacaoNotFoundException;
import com.ecoledger.movimentacao.application.service.ProducerNotApprovedException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    public record ProblemDetails(String type, String title, int status, String detail, String instance,
                                 OffsetDateTime timestamp, Map<String, String> errors) {
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetails> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage, (a, b) -> a));
        ProblemDetails p = new ProblemDetails(
                "https://example.com/probs/validation-error",
                "Validation Failed",
                HttpStatus.BAD_REQUEST.value(),
                "One or more validation errors occurred",
                request.getRequestURI(),
                OffsetDateTime.now(),
                errors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(p);
    }

    @ExceptionHandler(ProducerNotApprovedException.class)
    public ResponseEntity<ProblemDetails> handleProducerNotApproved(ProducerNotApprovedException ex, HttpServletRequest request) {
        ProblemDetails p = new ProblemDetails(
                "https://example.com/probs/producer-not-approved",
                "Producer Not Approved",
                HttpStatus.FORBIDDEN.value(),
                ex.getMessage(),
                request.getRequestURI(),
                OffsetDateTime.now(),
                Map.of()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(p);
    }

    @ExceptionHandler(InvalidAttachmentException.class)
    public ResponseEntity<ProblemDetails> handleInvalidAttachment(InvalidAttachmentException ex, HttpServletRequest request) {
        ProblemDetails p = new ProblemDetails(
                "https://example.com/probs/invalid-attachment",
                "Invalid Attachment",
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                request.getRequestURI(),
                OffsetDateTime.now(),
                Map.of()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(p);
    }

    @ExceptionHandler(MovimentacaoNotFoundException.class)
    public ResponseEntity<ProblemDetails> handleNotFound(MovimentacaoNotFoundException ex, HttpServletRequest request) {
        ProblemDetails p = new ProblemDetails(
                "https://example.com/probs/not-found",
                "Not Found",
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                request.getRequestURI(),
                OffsetDateTime.now(),
                Map.of()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(p);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetails> handleGeneric(Exception ex, HttpServletRequest request) {
        ProblemDetails p = new ProblemDetails(
                "https://example.com/probs/internal",
                "Internal Server Error",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ex.getMessage(),
                request.getRequestURI(),
                OffsetDateTime.now(),
                Map.of()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(p);
    }
}
