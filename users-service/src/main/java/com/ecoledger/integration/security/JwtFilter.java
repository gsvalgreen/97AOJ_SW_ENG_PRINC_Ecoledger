package com.ecoledger.integration.security;

import com.ecoledger.integration.security.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import io.jsonwebtoken.Claims;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            try {
                Claims claims = jwtService.parse(header);
                String sub = claims.getSubject();
                String scope = OptionalOf(claims, "scopes");
                List<SimpleGrantedAuthority> auth = Arrays.stream(scope.split(" "))
                        .filter(s -> !s.isBlank())
                        .map(s -> new SimpleGrantedAuthority("SCOPE_" + s))
                        .collect(Collectors.toList());
                var authentication = new UsernamePasswordAuthenticationToken(sub, null, auth);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                // invalid token -> clear context (unauthenticated)
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }

    private String OptionalOf(Claims claims, String key) {
        Object v = claims.get(key);
        if (v == null) return "";
        if (v instanceof String) return (String) v;
        return v.toString();
    }
}
