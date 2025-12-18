package com.ecoledger.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

class NotificationClientIT {

    @Test
    void notifyRegistration_sendsRequest() throws Exception {
        WireMockServer wm = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        wm.start();
        try {
            wm.stubFor(post(urlEqualTo("/notify")).willReturn(aResponse().withStatus(200)));

            WebClient.Builder builder = WebClient.builder();
            NotificationClient nc = new NotificationClient(builder, "http://localhost:" + wm.port() + "/notify");

            nc.notifyRegistration(Map.of("k", "v"));

            // async subscribe - give the client a moment to perform the request
            Thread.sleep(200);

            wm.verify(postRequestedFor(urlEqualTo("/notify")));
        } finally {
            wm.stop();
        }
    }
}
