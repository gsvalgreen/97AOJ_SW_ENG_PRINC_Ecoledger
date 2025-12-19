package com.ecoledger.integration.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class JwtFilterTest {

    @Test
    void optionalOf_behaviour() throws Exception {
        JwtService jwtService = Mockito.mock(JwtService.class);
        JwtFilter filter = new JwtFilter(jwtService);
        Claims claims = Mockito.mock(Claims.class);
        Mockito.when(claims.get("missing")).thenReturn(null);
        Mockito.when(claims.get("str")).thenReturn("abc");
        Mockito.when(claims.get("num")).thenReturn(123);

        Method m = JwtFilter.class.getDeclaredMethod("OptionalOf", Claims.class, String.class);
        m.setAccessible(true);
        assertEquals("", m.invoke(filter, claims, "missing"));
        assertEquals("abc", m.invoke(filter, claims, "str"));
        assertEquals("123", m.invoke(filter, claims, "num"));
    }
}
