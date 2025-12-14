package com.ecoledger.integration.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

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
}
