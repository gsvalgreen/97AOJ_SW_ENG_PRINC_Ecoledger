package com.ecoledger.movimentacao.application.service.impl;

import com.ecoledger.movimentacao.application.service.ProducerApprovalClient;
import com.ecoledger.movimentacao.config.ProducerApprovalProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;
import java.time.Duration;

@Component
public class HttpProducerApprovalClient implements ProducerApprovalClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpProducerApprovalClient.class);

    private final RestTemplate restTemplate;
    private final ProducerApprovalProperties properties;

    public HttpProducerApprovalClient(ProducerApprovalProperties properties) {
        this.properties = properties;
        this.restTemplate = createTemplate(properties);
    }

    @Override
    public boolean isApproved(String producerId) {
        var url = properties.baseUrl() + "/usuarios/" + producerId;
        try {
            var response = restTemplate.getForEntity(url, UsuarioResponse.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return false;
            }
            var usuario = response.getBody();
            return usuario.isProdutor() && usuario.isAprovado();
        } catch (RestClientException ex) {
            LOGGER.warn("Falha ao consultar usuario {} no serviço de usuários", producerId, ex);
            return false;
        }
    }

    private RestTemplate createTemplate(ProducerApprovalProperties properties) {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) properties.timeoutMs());
        factory.setReadTimeout((int) properties.timeoutMs());
        var template = new RestTemplate(factory);
        template.setErrorHandler(new NoopErrorHandler());
        return template;
    }

    private record UsuarioResponse(String role, String status) {
        boolean isProdutor() {
            return "produtor".equalsIgnoreCase(role);
        }

        boolean isAprovado() {
            return "APROVADO".equalsIgnoreCase(status);
        }
    }

    private static class NoopErrorHandler implements ResponseErrorHandler {
        @Override
        public boolean hasError(ClientHttpResponse response) {
            return false;
        }

        @Override
        public void handleError(ClientHttpResponse response) {
            // resposta delegada ao chamador
        }
    }
}
