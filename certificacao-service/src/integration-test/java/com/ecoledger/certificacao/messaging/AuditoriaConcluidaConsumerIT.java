package com.ecoledger.certificacao.messaging;

import com.ecoledger.certificacao.messaging.event.AuditoriaConcluidaEvent;
import com.ecoledger.certificacao.messaging.event.AuditoriaConcluidaEvent.DetalheEvidencia;
import com.ecoledger.certificacao.messaging.event.ResultadoAuditoria;
import com.ecoledger.certificacao.service.SeloService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest(properties = {"certificacao.kafka.enabled=true","certificacao.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}"})
@EmbeddedKafka(partitions = 1, topics = {"auditoria.concluida"})
@ActiveProfiles("test")
class AuditoriaConcluidaConsumerIT {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @SpyBean
    private SeloService seloService;

    @Test
    void whenAuditoriaConcluidaPublished_thenConsumerProcesses() {
        var event = new AuditoriaConcluidaEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "producer-it",
                ResultadoAuditoria.APROVADO,
                "v-it",
                List.of(new DetalheEvidencia("origem", "teste")),
                Instant.now()
        );

        kafkaTemplate.send("auditoria.concluida", event.producerId(), event);

        verify(seloService, timeout(5000)).processarAuditoriaConcluida(any(AuditoriaConcluidaEvent.class));
    }
}
