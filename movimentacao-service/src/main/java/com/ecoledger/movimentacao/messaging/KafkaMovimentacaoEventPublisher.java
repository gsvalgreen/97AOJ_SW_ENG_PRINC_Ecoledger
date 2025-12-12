package com.ecoledger.movimentacao.messaging;

import com.ecoledger.movimentacao.application.service.MovimentacaoEventPublisher;
import com.ecoledger.movimentacao.config.KafkaProperties;
import com.ecoledger.movimentacao.domain.model.Movimentacao;
import com.ecoledger.movimentacao.domain.model.MovimentacaoAnexo;
import com.ecoledger.movimentacao.messaging.event.MovimentacaoCriadaEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class KafkaMovimentacaoEventPublisher implements MovimentacaoEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaProperties kafkaProperties;

    public KafkaMovimentacaoEventPublisher(KafkaTemplate<String, Object> kafkaTemplate,
                                           KafkaProperties kafkaProperties) {
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaProperties = kafkaProperties;
    }

    @Override
    public void publishCreated(Movimentacao movimentacao) {
        if (!kafkaProperties.enabled()) {
            return;
        }
        MovimentacaoCriadaEvent event = toEvent(movimentacao);
        kafkaTemplate.send(kafkaProperties.topics().movimentacaoCriada(), movimentacao.getId().toString(), event);
    }

    private MovimentacaoCriadaEvent toEvent(Movimentacao movimentacao) {
        return new MovimentacaoCriadaEvent(
                movimentacao.getId(),
                movimentacao.getProducerId(),
                movimentacao.getCommodityId(),
                movimentacao.getTipo(),
                movimentacao.getQuantidade(),
                movimentacao.getUnidade(),
                movimentacao.getTimestamp(),
                movimentacao.getLatitude(),
                movimentacao.getLongitude(),
                movimentacao.getCriadoEm(),
                mapAnexos(movimentacao.getAnexos())
        );
    }

    private List<MovimentacaoCriadaEvent.MovimentacaoCriadaEventAnexo> mapAnexos(List<MovimentacaoAnexo> anexos) {
        return anexos.stream()
                .map(anexo -> new MovimentacaoCriadaEvent.MovimentacaoCriadaEventAnexo(
                        anexo.getTipo(),
                        anexo.getUrl(),
                        anexo.getHash()
                ))
                .collect(Collectors.toList());
    }
}
