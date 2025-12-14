package com.ecoledger.application.exception;

import com.ecoledger.application.dto.ErroResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.junit.jupiter.api.Assertions.*;

public class GlobalExceptionHandlerTest {

    @Test
    public void handle_validation_and_others() {
        GlobalExceptionHandler h = new GlobalExceptionHandler();

        var binding = new BeanPropertyBindingResult(new Object(), "obj");
        binding.addError(new FieldError("obj", "field", "must not be blank"));
        var ex = org.mockito.Mockito.mock(MethodArgumentNotValidException.class);
        org.mockito.Mockito.when(ex.getBindingResult()).thenReturn(binding);
        var resp = h.handleValidation(ex);
        assertEquals(400, resp.getStatusCodeValue());
        ErroResponseDto body = resp.getBody();
        assertNotNull(body);
        assertEquals("bad_request", body.codigo());
        assertTrue(body.mensagem().contains("field"));

        var r2 = h.handleIllegalArg(new IllegalArgumentException("not found"));
        assertEquals(404, r2.getStatusCodeValue());
        assertEquals("not_found", r2.getBody().codigo());

        var dive = new DataIntegrityViolationException("top", new RuntimeException("constraint fail"));
        var r3 = h.handleDataIntegrity(dive);
        assertEquals(409, r3.getStatusCodeValue());
        assertTrue(r3.getBody().mensagem().contains("constraint fail"));

        var r4 = h.handleGeneric(new Exception("boom"));
        assertEquals(500, r4.getStatusCodeValue());
        assertEquals("internal_error", r4.getBody().codigo());
    }
}
