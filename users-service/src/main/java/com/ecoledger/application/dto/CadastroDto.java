package com.ecoledger.application.dto;

import java.time.Instant;

public record CadastroDto(
        String cadastroId,
        String status,
        UsuarioDto candidatoUsuario,
        Instant submetidoEm
) {}
