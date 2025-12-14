package com.ecoledger.certificacao.dto;

import com.ecoledger.certificacao.model.SeloStatus;
import com.ecoledger.certificacao.model.SeloVerde;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta retornada ao solicitar recálculo do selo.")
public record RecalcularResponse(
        @Schema(description = "Status atualizado do selo após o recálculo.", allowableValues = {"ATIVO", "PENDENTE", "INATIVO"}, example = "ATIVO")
        SeloStatus novoStatus
) {
    public static RecalcularResponse from(SeloVerde selo) {
        return new RecalcularResponse(selo.getStatus());
    }
}
