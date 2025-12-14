package com.ecoledger.events;

import com.ecoledger.events.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class EventDtoAndPublisherTest {

    @Test
    public void dtos_and_publisher() throws Exception {
        var c = new CandidateUser("n","e","d","r");
        var a = new UserAttachment("tipo","url");
        var reg = new UserRegisteredEvent("cid", c, List.of(a), Instant.now());
        var st = new UserStatusEvent("cid", c, "APROVADO", "reason", Instant.now());

        KafkaTemplate<String,Object> kafka = mock(KafkaTemplate.class);
        ObjectMapper mapper = mock(ObjectMapper.class);
        when(mapper.writeValueAsString(any())).thenReturn("{}");

        EventPublisher pub = new EventPublisher(kafka, mapper);
        // exercise publishing methods
        pub.publishRegistered(reg);
        pub.publishApproved(st);
        pub.publishRejected(st);

        verify(mapper, atLeastOnce()).writeValueAsString(any());
        verify(kafka, atLeastOnce()).send(anyString(), any());

        // basic assertions on DTOs
        assertEquals("n", c.nome());
        assertEquals("url", a.url());
        assertNotNull(reg.submetidoEm());
        assertNotNull(st.timestamp());
    }

    @Test
    public void publish_throws_when_mapper_fails() throws Exception {
        KafkaTemplate<String,Object> kafka = mock(KafkaTemplate.class);
        ObjectMapper mapper = mock(ObjectMapper.class);
        when(mapper.writeValueAsString(any())).thenThrow(new RuntimeException("boom"));
        EventPublisher pub = new EventPublisher(kafka, mapper);
        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () -> pub.publishApproved(new Object()));
    }
}
