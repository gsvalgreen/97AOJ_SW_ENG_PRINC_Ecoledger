package com.ecoledger.certificacao.dto;

import com.ecoledger.certificacao.model.AlteracaoSelo;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Histórico de alterações do selo verde.")
public record HistoricoSeloResponse(
        @ArraySchema(arraySchema = @Schema(description = "Lista de alterações do selo."), schema = @Schema(implementation = AlteracaoSeloResponse.class))
        List<AlteracaoSeloResponse> items
) {
    public static HistoricoSeloResponse from(List<AlteracaoSelo> alteracoes) {
        return new HistoricoSeloResponse(alteracoes.stream().map(AlteracaoSeloResponse::from).toList());
    }
}
