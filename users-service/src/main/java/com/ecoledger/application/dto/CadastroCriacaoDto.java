package com.ecoledger.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

public record CadastroCriacaoDto(
        @NotBlank String nome,
        @Email @NotBlank String email,
        @NotBlank String documento,
        @NotBlank String senha,
        @NotBlank String role,
        @NotNull Map<String, Object> dadosFazenda,
        @NotNull List<Map<String, Object>> anexos
) {}
