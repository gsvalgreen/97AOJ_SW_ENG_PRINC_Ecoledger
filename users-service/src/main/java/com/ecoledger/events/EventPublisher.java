package com.ecoledger.events;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class EventPublisher {

    private final KafkaTemplate<String, Object> kafka;
    private final ObjectMapper mapper;

    public EventPublisher(KafkaTemplate<String, Object> kafka, ObjectMapper mapper) {
        this.kafka = kafka;
        this.mapper = mapper;
    }

    public void publishRegistered(Object payload) {
        publish("usuarios.registered", payload);
    }

    public void publishApproved(Object payload) {
        publish("usuarios.approved", payload);
    }

    public void publishRejected(Object payload) {
        publish("usuarios.rejected", payload);
    }

    private void publish(String topic, Object payload) {
        try {
            String json = mapper.writeValueAsString(payload);
            kafka.send(topic, json);
        } catch (Exception e) {
            throw new RuntimeException("Failed to publish event to topic " + topic, e);
        }
    }
}
