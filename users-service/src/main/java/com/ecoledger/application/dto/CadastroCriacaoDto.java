package com.ecoledger.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

@Schema(name = "CadastroCriacao", description = "Payload utilizado para criar um novo cadastro de usuário.")
public record CadastroCriacaoDto(
        @Schema(description = "Nome completo do usuário.", example = "João da Silva")
        @NotBlank String nome,
        @Schema(description = "Email de contato do usuário.", example = "joao@ecoledger.com.br", format = "email")
        @Email @NotBlank String email,
        @Schema(description = "Documento oficial (CPF/CNPJ) do usuário.", example = "12345678900")
        @NotBlank String documento,
        @NotBlank String senha,
        @Schema(description = "Perfil do usuário dentro da plataforma.", example = "produtor", allowableValues = {"produtor", "analista", "auditor"})
        @NotBlank String role,
        @Schema(description = "Dados complementares sobre a fazenda.", additionalProperties = Schema.AdditionalPropertiesValue.TRUE)
        @NotNull Map<String, Object> dadosFazenda,
        @Schema(description = "Lista de anexos enviados pelo usuário.", example = "[{\"tipo\":\"documento\",\"url\":\"https://storage/anexo.pdf\"}]")
        @NotNull List<Map<String, Object>> anexos
) {}
