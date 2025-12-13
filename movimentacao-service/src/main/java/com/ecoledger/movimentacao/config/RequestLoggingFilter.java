package com.ecoledger.movimentacao.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestLoggingFilter.class);
    public static final String TRACE_ID_HEADER = "X-Request-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String traceId = request.getHeader(TRACE_ID_HEADER);
        if (traceId == null || traceId.isBlank()) {
            traceId = request.getHeader("X-Correlation-Id");
        }
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString();
        }

        MDC.put("traceId", traceId);
        long start = System.nanoTime();
        try {
            LOGGER.info("Incoming request {} {} traceId={}", request.getMethod(), request.getRequestURI(), traceId);
            filterChain.doFilter(request, response);
            long durationMs = (System.nanoTime() - start) / 1_000_000;
            LOGGER.info("Completed request {} {} status={} durationMs={} traceId={}", request.getMethod(), request.getRequestURI(), response.getStatus(), durationMs, traceId);
        } catch (Exception ex) {
            LOGGER.error("Request {} {} failed traceId={}", request.getMethod(), request.getRequestURI(), traceId, ex);
            throw ex;
        } finally {
            MDC.remove("traceId");
        }
    }
}
