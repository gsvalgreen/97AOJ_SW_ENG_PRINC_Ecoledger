package com.ecoledger.integration.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.*;

@Component
public class JwtService {

    private final Key key;

    public JwtService(@Value("${jwt.secret:changeitchangeitchangeitchangeit}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public Claims parse(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token.replace("Bearer ", ""))
                .getBody();
    }
}

