package com.ecoledger.movimentacao.messaging;

import com.ecoledger.movimentacao.config.KafkaProperties;
import com.ecoledger.movimentacao.domain.model.Movimentacao;
import com.ecoledger.movimentacao.domain.model.MovimentacaoAnexo;
import com.ecoledger.movimentacao.messaging.event.MovimentacaoCriadaEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.eq;

class KafkaMovimentacaoEventPublisherTest {

    private KafkaTemplate<String, Object> kafkaTemplate;
    private KafkaMovimentacaoEventPublisher publisher;

    @BeforeEach
    void setup() {
        kafkaTemplate = mock(KafkaTemplate.class);
        KafkaProperties properties = new KafkaProperties(
                true,
                "localhost:9092",
                new KafkaProperties.Topics("movimentacao.criada", "movimentacao.atualizada"));
        publisher = new KafkaMovimentacaoEventPublisher(kafkaTemplate, properties);
    }

    @Test
    void shouldPublishMovimentacaoCriadaEvent() {
        Movimentacao movimentacao = buildMovimentacao();

        publisher.publishCreated(movimentacao);

        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(kafkaTemplate).send(eq("movimentacao.criada"), eq(movimentacao.getId().toString()), payloadCaptor.capture());

        assertThat(payloadCaptor.getValue())
                .isInstanceOf(MovimentacaoCriadaEvent.class)
                .extracting("movimentacaoId", "producerId", "commodityId")
                .containsExactly(movimentacao.getId(), movimentacao.getProducerId(), movimentacao.getCommodityId());
    }

    private Movimentacao buildMovimentacao() {
        Movimentacao movimentacao = new Movimentacao(
                "prod-1",
                "cmd-1",
                "COLHEITA",
                new BigDecimal("10"),
                "KG",
                OffsetDateTime.now(),
                -23.5,
                -46.6,
                List.of(buildAnexo())
        );
        movimentacao.setId(UUID.randomUUID());
        return movimentacao;
    }

    private MovimentacaoAnexo buildAnexo() {
        MovimentacaoAnexo anexo = new MovimentacaoAnexo();
        anexo.setTipo("application/pdf");
        anexo.setUrl("https://s3.local/doc.pdf");
        anexo.setHash("hash");
        return anexo;
    }
}
