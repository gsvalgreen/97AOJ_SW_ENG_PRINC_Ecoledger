package com.ecoledger.integration.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.security.Key;

import static org.junit.jupiter.api.Assertions.*;

public class JwtServiceTest {

    @Test
    public void parse_valid_token() {
        String secret = "changeitchangeitchangeitchangeit";
        JwtService s = new JwtService(secret);
        Key key = Keys.hmacShaKeyFor(secret.getBytes());
        String token = Jwts.builder().setSubject("sub").signWith(key).compact();
        var claims = s.parse("Bearer " + token);
        assertEquals("sub", claims.getSubject());
    }

    @Test
    public void generateAndValidateTokens() throws Exception {
        JwtService svc = new JwtService("changeitchangeitchangeitchangeit");

        Field f = JwtService.class.getDeclaredField("jwtExpiration");
        f.setAccessible(true);
        f.set(svc, 3600000L);

        Field f2 = JwtService.class.getDeclaredField("refreshExpiration");
        f2.setAccessible(true);
        f2.set(svc, 86400000L);

        String access = svc.generateAccessToken("user-123", "a@b", "ROLE_USER");
        assertNotNull(access);
        assertTrue(svc.validateToken(access));
        assertEquals("user-123", svc.extractUserId(access));

        String refresh = svc.generateRefreshToken("user-456");
        assertNotNull(refresh);
        assertTrue(svc.validateToken(refresh));

        assertEquals(3600L, svc.getExpirationInSeconds());
        assertFalse(svc.validateToken("invalid.token"));
    }
}
