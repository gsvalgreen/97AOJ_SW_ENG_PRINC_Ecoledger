package com.ecoledger.application.exception;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class GlobalExceptionHandlerTest {

    @Test
    public void handle_validation_and_others() {
        GlobalExceptionHandler h = new GlobalExceptionHandler();

        var binding = new BeanPropertyBindingResult(new Object(), "obj");
        binding.addError(new FieldError("obj","field","must not be blank"));
        var ex = org.mockito.Mockito.mock(MethodArgumentNotValidException.class);
        org.mockito.Mockito.when(ex.getBindingResult()).thenReturn(binding);
        var resp = h.handleValidation(ex);
        assertEquals(400, resp.getStatusCodeValue());
        Map<String,Object> body = resp.getBody();
        assertEquals("validation_failed", body.get("error"));
        assertTrue(((java.util.List<?>)body.get("details")).size() > 0);

        var r2 = h.handleIllegalArg(new IllegalArgumentException("not found"));
        assertEquals(404, r2.getStatusCodeValue());
        assertEquals("not_found", r2.getBody().get("error"));

        var dive = new DataIntegrityViolationException("top", new RuntimeException("constraint fail"));
        var r3 = h.handleDataIntegrity(dive);
        assertEquals(409, r3.getStatusCodeValue());
        assertTrue(((String)r3.getBody().get("message")).contains("constraint fail"));

        var r4 = h.handleGeneric(new Exception("boom"));
        assertEquals(500, r4.getStatusCodeValue());
        assertEquals("internal_error", r4.getBody().get("error"));
    }
}
