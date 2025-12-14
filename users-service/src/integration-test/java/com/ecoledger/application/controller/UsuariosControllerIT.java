// java
package com.ecoledger.application.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.ecoledger.repository.CadastroRepository;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UsuariosControllerIT {

    // declarar estático mas sem instanciar aqui (evita load-time init)
    static WireMockServer wireMock;

    @Autowired
    MockMvc mvc;

    @Autowired
    CadastroRepository cadastroRepository;

    ObjectMapper mapper = new ObjectMapper();

    // iniciar o WireMock aqui (executado antes do contexto Spring usar as properties)
    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        wireMock = new WireMockServer(options().dynamicPort());
        wireMock.start();
        r.add("notify.endpoint", () -> wireMock.baseUrl() + "/notify");
    }

    @BeforeAll
    void setupWiremock() {
        // criar stubs depois que o server estiver rodando
        wireMock.stubFor(WireMock.post(WireMock.urlEqualTo("/notify"))
                .willReturn(WireMock.aResponse().withStatus(200)));
    }

    @AfterAll
    static void stopWiremock() {
        if (wireMock != null) wireMock.stop();
    }

    @Test
    void whenRegister_thenReturn201_andPersisted() throws Exception {
        var payload = mapper.createObjectNode();
        payload.put("nome", "IT User");
        payload.put("email", "it@example.com");
        payload.put("documento", "11122233344");
        payload.put("role", "role-x");
        payload.set("dadosFazenda", mapper.createObjectNode().put("faz", "v"));
        payload.set("anexos", mapper.createArrayNode());

        var result = mvc.perform(post("/usuarios/cadastros")
                        .contentType(APPLICATION_JSON)
                        .header("Idempotency-Key", "it-key-1")
                        .content(payload.toString()))
                .andExpect(status().isCreated())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        JsonNode json = mapper.readTree(body);
        String cadastroId = json.get("cadastroId").asText();

        Assertions.assertTrue(cadastroRepository.findById(cadastroId).isPresent());
        wireMock.verify(WireMock.postRequestedFor(WireMock.urlEqualTo("/notify")));
    }
}
