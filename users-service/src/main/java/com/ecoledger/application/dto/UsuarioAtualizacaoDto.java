package com.ecoledger.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Map;

public record UsuarioAtualizacaoDto(
        @Size(min = 1, max = 300) String nome,
        @NotNull Map<String, Object> dadosFazenda
) {}
