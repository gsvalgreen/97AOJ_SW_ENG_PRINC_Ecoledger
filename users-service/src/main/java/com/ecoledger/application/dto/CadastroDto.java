package com.ecoledger.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(name = "Cadastro", description = "Detalhes completos do cadastro submetido.")
public record CadastroDto(
        @Schema(description = "Identificador único do cadastro.", example = "cad_123456")
        String cadastroId,
        @Schema(description = "Status atual do cadastro.", example = "APROVADO")
        String status,
        @Schema(description = "Perfil do usuário candidato.")
        UsuarioDto candidatoUsuario,
        @Schema(description = "Data e hora da submissão.", type = "string", format = "date-time")
        Instant submetidoEm
) {}
