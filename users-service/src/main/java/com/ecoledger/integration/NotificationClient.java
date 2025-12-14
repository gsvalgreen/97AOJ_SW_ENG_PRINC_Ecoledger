package com.ecoledger.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class NotificationClient {

    private final WebClient client;
    private final String notifyEndpoint;

    public NotificationClient(WebClient.Builder builder, @Value("${notify.endpoint:http://localhost:8081/notify}") String notifyEndpoint) {
        this.client = builder.build();
        this.notifyEndpoint = notifyEndpoint;
    }

    public void notifyApproval(Object payload) {
        send(payload);
    }

    public void notifyRejection(Object payload) {
        send(payload);
    }

    public void notifyRegistration(Object payload) {
        send(payload);
    }

    private void send(Object payload) {
        client.post()
                .uri(notifyEndpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(Void.class)
                .onErrorResume(e -> Mono.empty())
                .subscribe();
    }
}

