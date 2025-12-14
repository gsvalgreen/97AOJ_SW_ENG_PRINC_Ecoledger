package com.ecoledger.certificacao.dto;

import com.ecoledger.certificacao.model.AlteracaoSelo;
import com.ecoledger.certificacao.model.SeloStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(name = "AlteracaoSelo", description = "Representa uma alteração realizada no selo verde.")
public record AlteracaoSeloResponse(
        @Schema(description = "Identificador da alteração.", example = "4a1edcf8-1d3d-4f8a-9ca0-1ae95fc0a0bb")
        UUID id,
        @Schema(description = "Identificador do produtor vinculado à alteração.", example = "producer-001")
        String producerId,
        @Schema(description = "Status anterior do selo.", allowableValues = {"ATIVO", "PENDENTE", "INATIVO"})
        SeloStatus deStatus,
        @Schema(description = "Status resultante após a alteração.", allowableValues = {"ATIVO", "PENDENTE", "INATIVO"})
        SeloStatus paraStatus,
        @Schema(description = "Motivo registrado para a alteração.", example = "Pontuação abaixo do mínimo.")
        String motivo,
        @Schema(description = "Instante em que a alteração foi registrada.", type = "string", format = "date-time")
        Instant timestamp
) {
    public static AlteracaoSeloResponse from(AlteracaoSelo alteracao) {
        return new AlteracaoSeloResponse(
                alteracao.getId(),
                alteracao.getProducerId(),
                alteracao.getDeStatus(),
                alteracao.getParaStatus(),
                alteracao.getMotivo(),
                alteracao.getCreatedAt()
        );
    }
}
