# AGENTS.md — Orientações para agentes e desenvolvedores

## 1. Objetivo

Este documento padroniza a stack e as práticas de desenvolvimento para os microserviços do ECO LEDGER. O foco é Java 21 + Spring Boot, TDD obrigatório e uma estratégia de testes que cubra unidades e integrações (WireMock, H2, Spring Kafka Test).

## 2. Sumário (TL;DR)

- Stack: Java 21 + Spring Boot 3.5.8
- Build: Gradle (Kotlin DSL) + Gradle Wrapper
- Testes: JUnit 5, Mockito, WireMock, H2, Spring Kafka Test
- Convenção de testes: unit tests (`*Test.java`, task `test`), integration tests (`*IT.java`, task `integrationTest`)

---

## 3. Decisão de alto nível

### 3.1 Microserviços
- Cada microserviço é um projeto Gradle independente (repositório ou módulo).
- Cada projeto deve fornecer: `build.gradle.kts`, `settings.gradle.kts` e o Gradle Wrapper (`gradlew`, `gradlew.bat`, `gradle/wrapper/*`).

### 3.2 Branching e CI
- Branches: `main`, `develop`, `feature/*`.
- CI: executar `./gradlew clean build` (inclui testes unitários e de integração) em cada PR.

---

## 4. Estrutura de projeto recomendada

- src/main/java
- src/main/resources
- src/test/java (testes unitários)
- src/integration-test/java (testes de integração, convenção *IT.java)

---

## 5. Variáveis de ambiente comuns

- KAFKA_BOOTSTRAP — ex: `kafka:9092`
- SPRING_DATASOURCE_URL — ex: `jdbc:postgresql://users-db:5432/users`
- S3_ENDPOINT, S3_ACCESS_KEY, S3_SECRET_KEY — para MinIO
- NOTIFICATION_ENDPOINT — url do serviço de notificações

---

## 6. Build (Gradle Kotlin DSL)

### 6.1 Rationale
Usamos Gradle Kotlin DSL (`build.gradle.kts`) com Toolchain Java 21 e o Gradle Wrapper para garantir builds reprodutíveis.

### 6.2 Exemplo mínimo de `build.gradle.kts`

```kotlin
plugins {
    id("org.springframework.boot") version "3.5.8"
    id("io.spring.dependency-management") version "1.1.7"
    java
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.kafka:spring-kafka")
    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.mockito:mockito-junit-jupiter")
    // WireMock explicit version is acceptable; Spring Boot BOM will align Spring-related libs
    testImplementation("com.github.tomakehurst:wiremock-jre8:2.35.2")
    testImplementation("com.h2database:h2")
    testImplementation("org.springframework.kafka:spring-kafka-test")
}

// Configuração de sourceSet para testes de integração (convenção *IT)
sourceSets {
    create("integrationTest") {
        java.srcDir("src/integration-test/java")
        resources.srcDir("src/integration-test/resources")
        compileClasspath += sourceSets.main.get().output + sourceSets.test.get().output
        runtimeClasspath += output + compileClasspath
    }
}

val integrationTestImplementation by configurations.getting
val integrationTestRuntimeOnly by configurations.getting

configurations["integrationTestImplementation"].extendsFrom(configurations.testImplementation.get())
configurations["integrationTestRuntimeOnly"].extendsFrom(configurations.testRuntimeOnly.get())

// Tasks
val integrationTest = tasks.register<Test>("integrationTest") {
    description = "Runs integration tests"
    group = "verification"
    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath
    shouldRunAfter(tasks.test)
    useJUnitPlatform()
}

tasks.check {
    dependsOn(integrationTest)
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}
```

### 6.2.1 Compatibilidade de dependências

- Dependências gerenciadas pelo Spring Boot BOM (não é necessário declarar versão):
  - org.springframework.boot:spring-boot-starter-web
  - org.springframework.boot:spring-boot-starter-data-jpa
  - org.springframework.kafka:spring-kafka
  - org.springframework.boot:spring-boot-starter-test
  - org.springframework.kafka:spring-kafka-test

- Dependências não gerenciadas pelo BOM (recomendamos fixar versão compatível):
  - com.github.tomakehurst:wiremock-jre8: 2.35.2 (compatível com Java 21)
  - com.h2database:h2: use a série 2.1.x (testes em memória para JPA)
  - org.mockito:mockito-junit-jupiter: 5.x (Mockito 5 funciona bem com JUnit 5/Java 21)

> Observação: o plugin `org.springframework.boot` aplica um BOM que alinha as versões dos artefatos Spring entre si; ao atualizar `spring-boot` para 3.5.8, as versões transitivas do ecossistema Spring também serão compatíveis. Fixe versões explicitamente apenas para bibliotecas externas ao BOM quando necessário.

### 6.3 Gradle Wrapper (obrigatório)

Gere o wrapper localmente e comite os arquivos:

```bash
gradle wrapper --gradle-version 8.6
# commitar: gradlew, gradlew.bat, gradle/wrapper/gradle-wrapper.jar, gradle/wrapper/gradle-wrapper.properties
```

> Nota: as dependências do Spring (starters, spring-kafka, spring-boot-starter-test etc.) não têm versão explícita neste exemplo de `build.gradle.kts` porque o plugin `org.springframework.boot` fornece um BOM (Bill of Materials) que garante versões compatíveis entre si para a versão do Spring Boot declarada (3.5.8). Se precisar travar versões específicas para algum artefato não gerenciado pelo BOM (ex.: WireMock), declare a versão explicitamente.

---

## 7. Práticas de desenvolvimento (TDD)

- Todo código precisa ter teste unitário antes de implementação (prática TDD).
- Ciclo: escrever teste -> implementar -> refatorar.
- Nomeclatura de testes:
  - Unit: `ThingServiceTest` (task `test`).
  - Integration: `ThingControllerIT`, `ThingKafkaIT` (task `integrationTest`).
- Mock apenas dependências externas em unit tests; em integration tests use WireMock, H2 e Embedded Kafka.

---

## 8. Estratégia de testes e exemplos

### 8.1 Unit tests
- Ferramentas: JUnit 5 + Mockito.
- Exemplo:

```java
@ExtendWith(MockitoExtension.class)
class MovimentacaoServiceTest {

  @Mock
  private MovimentacaoRepository repo;

  @InjectMocks
  private MovimentacaoService service;

  @Test
  void shouldCreateMovimentacao_whenValidPayload() {
    // given
    var dto = new MovimentacaoDto(); // example DTO instance; replace with real fields in implementation
    when(repo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    // when
    var saved = service.create(dto);

    // then
    assertNotNull(saved.getId());
    verify(repo).save(any());
  }
}
```

### 8.2 Integration tests — REST endpoints (WireMock)
- Use WireMock para mockar APIs REST externas.
- Suba contexto Spring com `@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)` e `@AutoConfigureMockMvc`.

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class UsersControllerIT {

  @Autowired
  MockMvc mvc;

  static WireMockServer wireMock = new WireMockServer(options().dynamicPort());

  @BeforeAll
  static void startWiremock() { wireMock.start(); }
  @AfterAll
  static void stopWiremock() { wireMock.stop(); }

  @Test
  void whenRegister_thenReturn201_andEventPublished() throws Exception {
    wireMock.stubFor(post(urlEqualTo("/notify"))
      .willReturn(aResponse().withStatus(200)));

    mvc.perform(post("/cadastros")
      .contentType(MediaType.APPLICATION_JSON)
      .content("{}"))
      .andExpect(status().isCreated());
  }
}
```

### 8.3 Integration tests — JPA com H2
- Use `@DataJpaTest` ou `@SpringBootTest` com `application-test.yml` apontando para H2.

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:db;DB_CLOSE_DELAY=-1
    driver-class-name: org.h2.Driver
    username: sa
    password:
  jpa:
    hibernate:
      ddl-auto: create-drop
```

### 8.4 Kafka tests — Spring Kafka Test (EmbeddedKafka)
- Use `@EmbeddedKafka` e configure `spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}` no profile de teste.

```java
@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"movimentacao.events"})
class MovimentacaoKafkaIT {

  @Autowired
  private EmbeddedKafkaBroker embeddedKafkaBroker;

  @Autowired
  private KafkaTemplate<String, String> kafkaTemplate;

  @Test
  void whenProduce_thenConsumerReceives() throws Exception {
    kafkaTemplate.send("movimentacao.events", "key", "payload");
    // ...assert via consumer or using KafkaTestUtils.getSingleRecord
  }
}
```

---

## 9. Separação Unit vs Integration

- Unit tests: task `test`, arquivos sufixados `*Test.java`.
- Integration tests: task `integrationTest`, arquivos sufixados `*IT.java`.

---

## 10. CI e execução local

### 10.1 Comandos locais

```
# rodar unit tests
./gradlew test

# rodar unit + integration (integrationTest é executado pelo check)
./gradlew clean check

# build completo
./gradlew clean build
```

### 10.2 Recomendação para CI
- Use `./gradlew -B -Dorg.gradle.daemon=false clean build --no-daemon`.
- Habilite `--parallel` quando o runner tiver CPUs suficientes.

---

## 11. Observability e troubleshooting de testes

- Em `application-test.yml` mantenha logs reduzidos; registre apenas o necessário.
- Para falhas flakey, isole o teste e adicione retries se a infra externa for instável.

---

## 12. Boas práticas para agentes de IA

- Sempre começar por gerar testes (TDD).
- Preferir testes rápidos e estáveis.
- Mockar integrações externas em unit tests; usar WireMock/H2/EmbeddedKafka em integration.
- Documentar variáveis de ambiente e comandos para reproduzir localmente.
- Sempre rode os testes localmente antes de seguir para o próximo passo.

---

## 13. Template de PR e checklist mínimo

- Descrição breve e link para requisito/issue.
- Lista de testes incluídos (unit/integration).
- Comandos para reproduzir localmente.

Checklist mínimo:
- [ ] Unit tests passando
- [ ] Integration tests relevantes adicionados e passando
- [ ] Variáveis de ambiente documentadas
- [ ] Exemplos de requests ou Postman collection

---

## 14. Próximos passos (opcionais)

- Gerar um projeto template Gradle com `build.gradle.kts`, `settings.gradle.kts` e Gradle Wrapper.
- Criar exemplos reais (service + unit test + integration test) para `movimentacao-service`.
- Adicionar workflow de CI (GitHub Actions) com build e testes.

---

Documento criado para orientar agentes e desenvolvedores sobre stack, convenções e práticas de testes para os microserviços do ECO LEDGER.
