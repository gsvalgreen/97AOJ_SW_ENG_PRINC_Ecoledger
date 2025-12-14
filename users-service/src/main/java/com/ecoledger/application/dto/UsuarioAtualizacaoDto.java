package com.ecoledger.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Map;

@Schema(name = "UsuarioAtualizacao", description = "Payload para atualização parcial do usuário.")
public record UsuarioAtualizacaoDto(
        @Schema(description = "Nome completo atualizado do usuário.", example = "João Produtor")
        @Size(min = 1, max = 300) String nome,
        @Schema(description = "Dados da fazenda que podem ser atualizados.", additionalProperties = Schema.AdditionalPropertiesValue.TRUE)
        @NotNull Map<String, Object> dadosFazenda
) {}
