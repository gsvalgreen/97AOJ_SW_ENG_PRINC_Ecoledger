package com.ecoledger.application.dto;

import java.time.Instant;

public record UsuarioDto(
        String id,
        String nome,
        String email,
        String role,
        String documento,
        String status,
        Instant criadoEm
) {}
