package com.ecoledger.movimentacao.application.service.impl;

import com.ecoledger.movimentacao.application.service.ProducerApprovalClient;
import com.ecoledger.movimentacao.config.ProducerApprovalProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

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
        var traceId = MDC.get("traceId");
        var url = properties.baseUrl() + "/usuarios/" + producerId;
        LOGGER.info("Checking producer approval for {} traceId={} url={}", producerId, traceId, url);
        try {
            var response = restTemplate.getForEntity(url, UsuarioResponse.class);
            LOGGER.debug("Producer service responded with status={} body={}", response.getStatusCode().value(), response.getBody());
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                LOGGER.info("Producer {} not approved or no body returned traceId={}", producerId, traceId);
                return false;
            }
            var usuario = response.getBody();
            boolean approved = usuario.isProdutor() && usuario.isAprovado();
            LOGGER.info("Producer {} approval result={} traceId={}", producerId, approved, traceId);
            return approved;
        } catch (RestClientException ex) {
            LOGGER.warn("Falha ao consultar usuario {} no serviço de usuários traceId={}", producerId, MDC.get("traceId"), ex);
            return false;
        }
    }

    private RestTemplate createTemplate(ProducerApprovalProperties properties) {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) properties.timeoutMs());
        factory.setReadTimeout((int) properties.timeoutMs());
        var template = new RestTemplate(factory);
        if (properties.authToken() != null && !properties.authToken().isBlank()) {
            template.getInterceptors().add((request, body, execution) -> {
                request.getHeaders().set(HttpHeaders.AUTHORIZATION, "Bearer " + properties.authToken());
                return execution.execute(request, body);
            });
        }
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
