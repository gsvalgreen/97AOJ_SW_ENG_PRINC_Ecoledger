package com.ecoledger.movimentacao.application.controller;

import com.ecoledger.movimentacao.application.service.InvalidAttachmentException;
import com.ecoledger.movimentacao.application.service.MovimentacaoNotFoundException;
import com.ecoledger.movimentacao.application.service.ProducerNotApprovedException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.MethodParameter;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleProducerNotApproved_returnsForbiddenProblemDetails() {
        var ex = new ProducerNotApprovedException("p1");
        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        when(req.getRequestURI()).thenReturn("/test/prod");

        ResponseEntity<GlobalExceptionHandler.ProblemDetails> resp = handler.handleProducerNotApproved(ex, req);
        assertEquals(403, resp.getStatusCode().value());
        var body = resp.getBody();
        assertNotNull(body);
        assertEquals("Producer Not Approved", body.title());
        assertEquals("/test/prod", body.instance());
    }

    @Test
    void handleInvalidAttachment_returnsBadRequestProblemDetails() {
        var ex = new InvalidAttachmentException("bad file");
        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        when(req.getRequestURI()).thenReturn("/test/attach");

        ResponseEntity<GlobalExceptionHandler.ProblemDetails> resp = handler.handleInvalidAttachment(ex, req);
        assertEquals(400, resp.getStatusCode().value());
        var body = resp.getBody();
        assertNotNull(body);
        assertEquals("Invalid Attachment", body.title());
        assertEquals("bad file", body.detail());
    }

    @Test
    void handleNotFound_returnsNotFoundProblemDetails() {
        var ex = new MovimentacaoNotFoundException(java.util.UUID.fromString("00000000-0000-0000-0000-000000000001"));
        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        when(req.getRequestURI()).thenReturn("/test/notfound");

        ResponseEntity<GlobalExceptionHandler.ProblemDetails> resp = handler.handleNotFound(ex, req);
        assertEquals(404, resp.getStatusCode().value());
        var body = resp.getBody();
        assertNotNull(body);
        assertEquals("Not Found", body.title());
    }

    @Test
    void handleGeneric_returnsInternalServerErrorProblemDetails() {
        var ex = new RuntimeException("oops");
        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        when(req.getRequestURI()).thenReturn("/test/err");

        ResponseEntity<GlobalExceptionHandler.ProblemDetails> resp = handler.handleGeneric(ex, req);
        assertEquals(500, resp.getStatusCode().value());
        var body = resp.getBody();
        assertNotNull(body);
        assertEquals("Internal Server Error", body.title());
        assertEquals("oops", body.detail());
    }

    @Test
    void handleValidation_buildsProblemDetailsWithFieldErrors() throws NoSuchMethodException {
        // prepare a MethodArgumentNotValidException with a binding result that has field errors
        Method method = this.getClass().getDeclaredMethod("dummy", String.class);
        MethodParameter mp = new MethodParameter(method, 0);
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "obj");
        bindingResult.addError(new FieldError("obj", "field1", "must not be blank"));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(mp, bindingResult);
        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        when(req.getRequestURI()).thenReturn("/test/validate");

        ResponseEntity<GlobalExceptionHandler.ProblemDetails> resp = handler.handleValidation(ex, req);
        assertEquals(400, resp.getStatusCode().value());
        var body = resp.getBody();
        assertNotNull(body);
        Map<String, String> errors = body.errors();
        assertEquals(1, errors.size());
        assertEquals("must not be blank", errors.get("field1"));
    }


    @SuppressWarnings("unused")
    private void dummy(String s) {
        // dummy method used to create MethodParameter
    }
}
