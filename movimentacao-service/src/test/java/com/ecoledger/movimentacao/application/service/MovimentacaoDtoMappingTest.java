package com.ecoledger.movimentacao.application.service;

import com.ecoledger.movimentacao.application.dto.MovimentacaoDetailResponse;
import com.ecoledger.movimentacao.domain.model.Movimentacao;
import com.ecoledger.movimentacao.domain.model.MovimentacaoAnexo;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MovimentacaoDtoMappingTest {

    @Test
    void shouldMapEntityToDetailResponse() {
        MovimentacaoAnexo anexo = new MovimentacaoAnexo();
        anexo.setTipo("image/png");
        anexo.setUrl("https://example.com/a.png");
        anexo.setHash("abc123");

        Movimentacao m = new Movimentacao(
                "prod-1",
                "cmd-1",
                "COLHEITA",
                new BigDecimal("2.5"),
                "KG",
                OffsetDateTime.now(),
                10.0,
                20.0,
                List.of(anexo)
        );

        MovimentacaoDetailResponse dto = MovimentacaoDetailResponse.fromEntity(m);

        assertThat(dto.producerId()).isEqualTo("prod-1");
        assertThat(dto.commodityId()).isEqualTo("cmd-1");
        assertThat(dto.tipo()).isEqualTo("COLHEITA");
        assertThat(dto.quantidade()).isEqualByComparingTo(new BigDecimal("2.5"));
        assertThat(dto.unidade()).isEqualTo("KG");
        assertThat(dto.localizacao()).isNotNull();
        assertThat(dto.localizacao().lat()).isEqualTo(10.0);
        assertThat(dto.localizacao().lon()).isEqualTo(20.0);
        assertThat(dto.anexos()).hasSize(1);
        assertThat(dto.anexos().get(0).tipo()).isEqualTo("image/png");
        assertThat(dto.anexos().get(0).url()).isEqualTo("https://example.com/a.png");
        assertThat(dto.anexos().get(0).hash()).isEqualTo("abc123");
    }
}
