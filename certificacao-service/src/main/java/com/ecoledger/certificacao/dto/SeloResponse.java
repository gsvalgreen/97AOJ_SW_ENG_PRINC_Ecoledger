package com.ecoledger.certificacao.dto;

import com.ecoledger.certificacao.model.SeloNivel;
import com.ecoledger.certificacao.model.SeloStatus;
import com.ecoledger.certificacao.model.SeloVerde;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

@Schema(name = "SeloVerde", description = "Detalhes do selo verde atribuído a um produtor.")
public record SeloResponse(
        @Schema(description = "Identificador do produtor.", example = "producer-001")
        String producerId,
        @Schema(description = "Status atual do selo.", allowableValues = {"ATIVO", "PENDENTE", "INATIVO"}, example = "ATIVO")
        SeloStatus status,
        @Schema(description = "Nível do selo com base na pontuação.", allowableValues = {"BRONZE", "PRATA", "OURO"}, example = "PRATA")
        SeloNivel nivel,
        @Schema(description = "Pontuação consolidada do produtor.", example = "850")
        int pontuacao,
        @Schema(description = "Lista de motivos que explicam o status atual.")
        List<String> motivos,
        @Schema(description = "Data/hora da última verificação.", type = "string", format = "date-time")
        Instant ultimoCheck
) {
    public static SeloResponse from(SeloVerde selo) {
        return new SeloResponse(
                selo.getProducerId(),
                selo.getStatus(),
                selo.getNivel(),
                selo.getPontuacao(),
                selo.getMotivos(),
                selo.getUltimoCheck()
        );
    }
}
