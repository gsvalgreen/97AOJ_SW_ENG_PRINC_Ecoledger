package com.ecoledger.application.controller;

import com.ecoledger.config.TestKafkaConfig;
import com.ecoledger.repository.CadastroRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.util.Map;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, topics = {"usuarios.registered", "usuarios.approved", "usuarios.rejected"})
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}"
})
@Import(TestKafkaConfig.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UsuariosControllerIT {

    // WireMock server started dynamically and property injected so NotificationClient uses it
    static WireMockServer wireMock;

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        wireMock = new WireMockServer(options().dynamicPort());
        wireMock.start();
        r.add("notify.endpoint", () -> wireMock.baseUrl() + "/notify");
    }

    @BeforeAll
    void setupWiremock() {
        wireMock.stubFor(WireMock.post(WireMock.urlEqualTo("/notify"))
                .willReturn(WireMock.aResponse().withStatus(200)));
    }

    @AfterAll
    static void stopWiremock() {
        if (wireMock != null) wireMock.stop();
    }

    @Autowired
    MockMvc mvc;

    @Autowired
    CadastroRepository cadastroRepository;

    @Autowired
    EmbeddedKafkaBroker embeddedKafka;

    ObjectMapper mapper = new ObjectMapper();

    @Test
    void whenRegister_thenReturn201_andPersisted() throws Exception {
        var payload = mapper.createObjectNode();
        payload.put("nome", "IT User");
        payload.put("email", "it@example.com");
        payload.put("documento", "11122233344");
        payload.put("senha", "senha123");
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

        Assertions.assertTrue(cadastroRepository.findById(java.util.UUID.fromString(cadastroId)).isPresent());

        // await both the HTTP notification and the Kafka event
        Awaitility.await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            // verify WireMock received the POST
            wireMock.verify(WireMock.postRequestedFor(WireMock.urlEqualTo("/notify")));

            // verify Kafka message was published to usuarios.registered
            Map<String, Object> props = Map.of(
                    ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafka.getBrokersAsString(),
                    ConsumerConfig.GROUP_ID_CONFIG, "users-it-consumer",
                    ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"
            );
            var factory = new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new StringDeserializer());
            var consumer = factory.createConsumer();
            embeddedKafka.consumeFromAnEmbeddedTopic(consumer, "usuarios.registered");
            var records = consumer.poll(java.time.Duration.ofSeconds(1));
            consumer.close();

            assertThat(records).hasSizeGreaterThan(0);
            var record = records.iterator().next();
            assertThat(record.value()).contains(cadastroId);
        });
    }
}
