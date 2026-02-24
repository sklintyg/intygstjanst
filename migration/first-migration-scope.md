# Intygstjänst — First Migration Scope

*Target: Spring Boot application with no XML configuration, Spring MVC REST, no `se.inera.intyg.infra` dependencies, JUnit 5 only, and
Spring Boot structured ECS logging.*

---

## 1. Objective

Complete the first migration step towards the [goal tech stack](goal-tech-stack.md). After this migration, intygstjänst will:

1. **Run as a Spring Boot application** — executable JAR with embedded Tomcat (no external WAR deployment).
2. **Use Spring Boot starters and auto-configuration** — replacing manual bean wiring wherever possible.
3. **Have all bean configuration in Java code** — zero XML-based Spring configuration.
4. **Expose REST APIs via Spring MVC** — replacing JAX-RS (`jakarta.ws.rs`) endpoints.
5. **Have no dependencies on `se.inera.intyg.infra`** — all infra functionality either inlined, replaced, or accessed via REST APIs.
6. **Use JUnit Jupiter exclusively** — no JUnit 4 tests or vintage engine.
7. **Use Spring Boot structured logging in ECS format** — replacing the manual logback-ecs-encoder setup.

> **Explicitly out of scope for this migration:** module restructuring (hexagonal/domain module), Testcontainers, MapStruct, Gradle Kotlin
> DSL migration, WSDL2Java plugin migration, and any changes to `se.inera.intyg.common` or schema library dependencies.

---

## 2. Current State Summary

| Aspect                          | Current State                                                                                                                                                                                                                                                    |
|---------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Application type**            | Traditional Spring Framework WAR deployed on external Tomcat 10 via Gretty                                                                                                                                                                                       |
| **REST APIs**                   | JAX-RS (`jakarta.ws.rs`) routed through CXF `jaxrs:server` (~15 controller files)                                                                                                                                                                                |
| **SOAP APIs**                   | Apache CXF (`jaxws:endpoint`) configured in `application-context-ws.xml` (~20 endpoints)                                                                                                                                                                         |
| **Bean configuration**          | Mix of XML files (`application-context.xml`, `application-context-ws.xml`, `application-context-ws-stub.xml`, `jaxrs-context.xml`, `persistence.xml`, `web.xml`) and Java `@Configuration` classes (4 classes)                                                   |
| **`se.inera.intyg.infra` deps** | 10 modules: `certificate`, `hsa-integration-api`, `pu-integration-api`, `intyginfo`, `message`, `monitoring`, `security-filter`, `sjukfall-engine`, `testcertificate`, + 2 runtime (`hsa-integration-intyg-proxy-service`, `pu-integration-intyg-proxy-service`) |
| **Test framework**              | Mix of JUnit 4 (~100 files) and JUnit 5 (~63 files); `junit-vintage-engine` in both `web` and `persistence`                                                                                                                                                      |
| **Logging**                     | Logback + `co.elastic.logging:logback-ecs-encoder` with manual `LogbackConfiguratorContextListener` and custom `logback-spring-base.xml`                                                                                                                         |
| **Metrics**                     | Prometheus `simpleclient_servlet` with manual servlet mapping                                                                                                                                                                                                    |
| **Persistence config**          | Manual `JpaConfigBase` with explicit DataSource, EntityManagerFactory, TransactionManager, and Liquibase beans                                                                                                                                                   |
| **JMS config**                  | Manual ActiveMQ `ConnectionFactory`, `PooledConnectionFactory`, and `JmsTemplate` beans in `JmsConfig`                                                                                                                                                           |

---

## 3. Migration Work Items

### 3.1 Spring Boot Application Bootstrap

**What changes:**

- Create a `@SpringBootApplication` main class (e.g., `IntygstjanstApplication`).
- Convert the `web` module from a WAR (Gretty/Tomcat plugin) to an executable JAR with the Spring Boot Gradle plugin.
- Replace `web.xml` servlet/filter/listener declarations with Spring Boot auto-configuration and `FilterRegistrationBean`/
  `ServletRegistrationBean` where needed.
- Remove the Gretty plugin configuration from `web/build.gradle`.
- Update the `Dockerfile` to use a Spring Boot JAR base image instead of deploying a WAR into Catalina.

**Files affected:**

- `web/build.gradle` — switch from `war` + `org.gretty` plugins to `org.springframework.boot` plugin
- `build.gradle` — add Spring Boot plugin to the root project
- New: `web/src/main/java/.../IntygstjanstApplication.java`
- Remove: `web/src/main/webapp/WEB-INF/web.xml`
- `Dockerfile` — change from WAR/Catalina to JAR-based

**Spring Boot starters to add:**

- `spring-boot-starter-web` (embedded Tomcat + Spring MVC + Jackson)
- `spring-boot-starter-data-jpa` (replaces manual JPA/Hibernate/HikariCP config)
- `spring-boot-starter-activemq` (replaces manual ActiveMQ connection factory config)
- `spring-boot-starter-actuator` (replaces manual Prometheus servlet)
- `spring-boot-starter-data-redis` (replaces manual Redis/Jedis config)

**Dependencies to remove (replaced by starters):**

- `org.springframework:spring-webmvc` (provided by `starter-web`)
- `org.springframework:spring-jms` (provided by `starter-activemq`)
- `org.springframework.data:spring-data-jpa` (provided by `starter-data-jpa`)
- `com.zaxxer:HikariCP` (auto-configured by `starter-data-jpa`)
- `io.prometheus:simpleclient_servlet` (replaced by Actuator + Micrometer)
- `ch.qos.logback:logback-classic` (provided by `starter-web`)
- `org.slf4j:jul-to-slf4j` (auto-configured by Spring Boot)
- `com.fasterxml.jackson.jakarta.rs:jackson-jakarta-rs-json-provider` (replaced by Spring MVC Jackson auto-config)
- `jakarta.ws.rs:jakarta.ws.rs-api` (no longer needed after JAX-RS removal)
- `jakarta.transaction:jakarta.transaction-api` (provided by `starter-data-jpa`)

---

### 3.2 Eliminate All XML Bean Configuration

**What changes:**

Each XML configuration file must be converted to Java `@Configuration` classes or removed entirely when Spring Boot auto-configuration
covers the concern.

| XML File                          | Action                                                                                                                                                      |
|-----------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `application-context.xml`         | **Remove.** Replace with component scanning via `@SpringBootApplication`, `@Import`, and Java config classes.                                               |
| `application-context-ws.xml`      | **Convert** to a Java `@Configuration` class (e.g., `CxfEndpointConfig`) that programmatically registers all CXF `jaxws:endpoint` and `jaxws:client` beans. |
| `application-context-ws-stub.xml` | **Convert** to a Java `@Configuration` class with `@Profile("it-fk-stub")`.                                                                                 |
| `jaxrs-context.xml`               | **Remove entirely.** The JAX-RS endpoints will be rewritten as Spring MVC controllers (see §3.3).                                                           |
| `META-INF/persistence.xml`        | **Remove.** Spring Boot auto-configuration with `@EntityScan` replaces the explicit persistence unit declaration.                                           |
| `web.xml`                         | **Remove.** Spring Boot embedded server replaces all servlet/filter/listener declarations.                                                                  |
| `logback-spring-base.xml`         | **Remove.** Replaced by Spring Boot's native ECS support (see §3.7).                                                                                        |
| `META-INF/cxf/cxf.xml` (imported) | **Keep** — this is a CXF framework resource. The `@ImportResource` for it moves into the new CXF config class.                                              |
| `test-application-context.xml`    | **Remove.** Replace with `@SpringBootTest` or explicit `@Configuration` test classes.                                                                       |

**Classpath XML imports from `se.inera.intyg.infra` and `se.inera.intyg.common` to handle:**

- `classpath:basic-cache-config.xml` — Replace with Spring Boot Redis auto-configuration.
- `classpath:/hsa-integration-intyg-proxy-service-config.xml` — Remove (infra dependency removed; see §3.5).
- `classpath:/pu-integration-intyg-proxy-service-config.xml` — Remove (infra dependency removed; see §3.5).
- `classpath:common-config.xml` — Evaluate contents; inline needed beans.
- `classpath*:module-config.xml` — Evaluate contents; inline needed beans.
- `classpath*:it-module-cxf-servlet.xml` — Evaluate contents; inline needed beans.

---

### 3.3 REST APIs: JAX-RS → Spring MVC

**What changes:**

All REST controllers currently using JAX-RS annotations must be converted to Spring MVC annotations.

| JAX-RS                                  | Spring MVC                                                         |
|-----------------------------------------|--------------------------------------------------------------------|
| `@Path("/...")`                         | `@RestController` + `@RequestMapping("/...")`                      |
| `@GET`                                  | `@GetMapping`                                                      |
| `@POST`                                 | `@PostMapping`                                                     |
| `@PUT`                                  | `@PutMapping`                                                      |
| `@DELETE`                               | `@DeleteMapping`                                                   |
| `@Produces(MediaType.APPLICATION_JSON)` | `produces = MediaType.APPLICATION_JSON_VALUE` (or rely on default) |
| `@Consumes(MediaType.APPLICATION_JSON)` | `consumes = MediaType.APPLICATION_JSON_VALUE` (or rely on default) |
| `@PathParam`                            | `@PathVariable`                                                    |
| `@QueryParam`                           | `@RequestParam`                                                    |
| `jakarta.ws.rs.core.Response`           | `ResponseEntity<T>`                                                |

**Controllers to migrate (~15 files):**

*Internal API controllers:*

- `CitizenController` (`/internalapi/citizens`)
- `IntygInfoController` (`/internalapi/intyginfo`)
- `TestCertificateController` (`/internalapi/testcertificate`)
- `MessageController` (`/internalapi/message`)
- `TypedCertificateController` (`/internalapi/typedcertificate`)
- `SickLeaveController` (`/internalapi/sickleave`)
- `RekoController` (`/internalapi/reko`)
- `CitizenCertificateController` (`/internalapi/citizencertificate`)
- `CertificateListController` (`/internalapi/certificatelist`)
- `CertificateExportController` (`/internalapi/certificateexport`)

*Testability/stub controllers (profile-gated):*

- `SendMessageToCareResponderStubRestApi` (`/api/send-message-to-care`)
- `CertificateResource` (`/resources/...`)
- `SjukfallCertResource` (`/resources/...`)
- `StatisticsServiceResource` (`/resources/...`)
- `FkStubResource` (`/resources/...`)
- `TestabilityController` (`/resources/...`)

**Internal API filter:** The `InternalApiFilter` (from `se.inera.intyg.infra.security.filter`) currently restricts `/internalapi/*` by
source port. This must be replaced with a local implementation registered as a Spring Boot `FilterRegistrationBean`.

**Notes:**

- The SOAP endpoints (CXF `jaxws:endpoint`) are **not** being converted to Spring MVC in this scope. They remain as CXF endpoints but
  configured via Java instead of XML.
- The CXF `jaxrs:server` beans in `jaxrs-context.xml` are removed entirely since all REST endpoints move to Spring MVC.

---

### 3.4 Auto-Configuration Replacements

**What changes:**

Replace manually configured beans with Spring Boot auto-configuration.

#### 3.4.1 JPA / DataSource (replaces `JpaConfigBase` + `JpaConfig`)

- **Remove:** `JpaConfigBase`, `JpaConfig`, `JpaConstants`, `persistence.xml`.
- **Replace with:** `spring-boot-starter-data-jpa` auto-configuration.
- **Configure via `application.properties`:**
  ```properties
  spring.datasource.url=...
  spring.datasource.username=...
  spring.datasource.password=...
  spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
  spring.datasource.hikari.maximum-pool-size=...
  spring.jpa.hibernate.ddl-auto=...
  spring.jpa.properties.hibernate.dialect=...
  ```
- **Liquibase:** Auto-configured by Spring Boot when Liquibase is on the classpath. Set
  `spring.liquibase.change-log=classpath:changelog/changelog.xml`.
- **Entity scanning:** Use `@EntityScan` on the main application class to cover `se.inera.intyg.intygstjanst.persistence.model.dao`.

#### 3.4.2 JMS / ActiveMQ (replaces `JmsConfig`)

- **Remove:** The manual `ActiveMQConnectionFactory`, `PooledConnectionFactory`, `JmsTemplate`, `JmsTransactionManager`, and `Queue` beans.
- **Replace with:** `spring-boot-starter-activemq` auto-configuration.
- **Configure via `application.properties`:**
  ```properties
  spring.activemq.broker-url=...
  spring.activemq.user=...
  spring.activemq.password=...
  spring.activemq.pool.enabled=true
  ```
- **JMS listener factory:** Auto-configured by Spring Boot. Custom settings (concurrency, caching) can be set via properties.

#### 3.4.3 Metrics / Health (replaces Prometheus servlet)

- **Remove:** `io.prometheus:simpleclient_servlet` dependency and the manual `<servlet>` mapping in `web.xml`.
- **Replace with:** `spring-boot-starter-actuator` with Micrometer Prometheus registry.
- **Configure via `application.properties`:**
  ```properties
  management.endpoints.web.exposure.include=health,info,prometheus
  management.metrics.export.prometheus.enabled=true
  ```

#### 3.4.4 Redis / Caching

- **Replace** manual Jedis/Redis configuration (used in `JobConfig` and imported via `basic-cache-config.xml`) with
  `spring-boot-starter-data-redis` auto-configuration.
- **Configure via `application.properties`:**
  ```properties
  spring.data.redis.host=...
  spring.data.redis.port=...
  spring.data.redis.password=...
  ```

---

### 3.5 Remove All `se.inera.intyg.infra` Dependencies

**What changes:**

Every `se.inera.intyg.infra` dependency must be removed. The functionality they provide is either inlined, replaced with standard Spring
Boot equivalents, or accessed via REST API calls to `intyg-proxy-service`.

| Infra Module                                                                        | Files Using It                                        | Replacement Strategy                                                                                                                                                                                                                                                                                              |
|-------------------------------------------------------------------------------------|-------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **`monitoring`** (~34 files)                                                        | Controllers, services                                 | **Inline.** Copy the `@PrometheusTimeMethod` annotation and its AOP aspect into the project. Replace with Micrometer `@Timed` if simple timing is sufficient. Replace `MonitoringConfiguration` import with local config. Replace `LogMarkers` usage with the local `LogMarkers` already in the `logging` module. |
| **`sjukfall-engine`** (~41 files)                                                   | Sick leave calculation services                       | **Inline.** Copy the required DTOs (`IntygData`, `SjukfallEnhet`, `DiagnosKapitel`, `Formaga`, `Lakare`, `RekoStatusDTO`, etc.) and the `SjukfallEngineServiceImpl` into the project. This is core business logic for intygstjänst.                                                                               |
| **`certificate`** (~11 files)                                                       | Certificate list/typed certificate services           | **Inline.** Copy the DTOs (`CertificateListEntry`, `CertificateListRequest`, `CertificateListResponse`, `DiagnosedCertificate`, `SickLeaveCertificate`, `TypedCertificateRequest`, `SickLeaveCertificateBuilder`) into the project.                                                                               |
| **`hsa-integration-api`** + runtime `hsa-integration-intyg-proxy-service` (~1 file) | HSA employee/org lookups                              | **Replace with REST client.** Call `intyg-proxy-service` REST API directly using Spring's `RestClient`. Remove the SOAP-based HSA integration entirely.                                                                                                                                                           |
| **`pu-integration-api`** + runtime `pu-integration-intyg-proxy-service` (~3 files)  | Person data lookups (PU)                              | **Replace with REST client.** Call `intyg-proxy-service` REST API directly using Spring's `RestClient`. Define local DTOs for the response.                                                                                                                                                                       |
| **`intyginfo`** (~2 files)                                                          | Intyg info event reporting                            | **Inline.** Copy the DTOs (`IntygInfoEvent`, `ItIntygInfo`, `IntygInfoEventType`) into the project.                                                                                                                                                                                                               |
| **`message`** (~1 file)                                                             | Message DTO                                           | **Inline.** Copy `MessageFromIT` DTO into the project.                                                                                                                                                                                                                                                            |
| **`testcertificate`** (~3 files)                                                    | Test certificate erase functionality                  | **Inline.** Copy `TestCertificateEraseRequest` and `TestCertificateEraseResult` DTOs into the project.                                                                                                                                                                                                            |
| **`security-filter`** (~1 file)                                                     | `InternalApiFilter` for internal API port restriction | **Inline.** Implement a simple local `InternalApiFilter` as a Spring `OncePerRequestFilter` and register it via `FilterRegistrationBean`.                                                                                                                                                                         |

**Dependency lines to remove from `web/build.gradle`:**

```groovy
// All of these are removed:
implementation "se.inera.intyg.infra:certificate:${infraVersion}"
implementation "se.inera.intyg.infra:hsa-integration-api:${infraVersion}"
implementation "se.inera.intyg.infra:pu-integration-api:${infraVersion}"
implementation "se.inera.intyg.infra:intyginfo:${infraVersion}"
implementation "se.inera.intyg.infra:message:${infraVersion}"
implementation "se.inera.intyg.infra:monitoring:${infraVersion}"
implementation "se.inera.intyg.infra:security-filter:${infraVersion}"
implementation "se.inera.intyg.infra:sjukfall-engine:${infraVersion}"
implementation "se.inera.intyg.infra:testcertificate:${infraVersion}"
runtimeOnly "se.inera.intyg.infra:hsa-integration-intyg-proxy-service:${infraVersion}"
runtimeOnly "se.inera.intyg.infra:pu-integration-intyg-proxy-service:${infraVersion}"
```

**The `infraVersion` property can be removed from `build.gradle` entirely.**

---

### 3.6 Migrate All Tests to JUnit Jupiter

**What changes:**

All remaining JUnit 4 tests must be migrated to JUnit 5 (Jupiter). Based on the codebase analysis, there are approximately **100 test files
** still using JUnit 4 imports across the `web` (~90) and `persistence` (~10) modules.

**Migration pattern per file:**

| JUnit 4                                   | JUnit 5                                                   |
|-------------------------------------------|-----------------------------------------------------------|
| `import org.junit.Test`                   | `import org.junit.jupiter.api.Test`                       |
| `import org.junit.Before`                 | `import org.junit.jupiter.api.BeforeEach`                 |
| `import org.junit.After`                  | `import org.junit.jupiter.api.AfterEach`                  |
| `import org.junit.BeforeClass`            | `import org.junit.jupiter.api.BeforeAll`                  |
| `import org.junit.Assert.*`               | `import org.junit.jupiter.api.Assertions.*` (or AssertJ)  |
| `import org.junit.runner.RunWith`         | `import org.junit.jupiter.api.extension.ExtendWith`       |
| `@RunWith(MockitoJUnitRunner.class)`      | `@ExtendWith(MockitoExtension.class)`                     |
| `@RunWith(SpringJUnit4ClassRunner.class)` | `@ExtendWith(SpringExtension.class)` or `@SpringBootTest` |
| `@Rule ExpectedException`                 | `assertThrows(...)`                                       |

**Dependency changes:**

In `persistence/build.gradle`:

```groovy
// Remove:
testImplementation "junit:junit"
testRuntimeOnly "org.junit.vintage:junit-vintage-engine"

// Add:
testImplementation "org.junit.jupiter:junit-jupiter"
testImplementation "org.mockito:mockito-junit-jupiter"
```

In `web/build.gradle`:

```groovy
// Remove:
testRuntimeOnly "org.junit.vintage:junit-vintage-engine"
```

**Test XML context:** Remove `test-application-context.xml` and replace with `@SpringBootTest` or direct `@Configuration` inner classes in
the test classes.

---

### 3.7 Logging: Spring Boot Structured ECS Logging

**What changes:**

Replace the manual Logback ECS encoder setup with Spring Boot 3.4+'s native structured logging support.

**Remove:**

- `co.elastic.logging:logback-ecs-encoder` dependency from `web/build.gradle`
- `web/src/main/resources/logback/logback-spring-base.xml` (defines the `ECS_JSON_CONSOLE` appender)
- `devops/dev/config/logback-spring.xml` (manual Logback configuration)
- `LogbackConfiguratorContextListener` class (manual Logback initialization from servlet context)
- The `logbackConfigParameter` context-param from `web.xml` (already removed with `web.xml`)

**Replace with `application.properties`:**

```properties
logging.structured.format.console=ecs
logging.structured.ecs.service.name=intygstjanst
logging.structured.ecs.service.environment=${spring.profiles.active:default}
```

**Retain:**

- The `logging` module's `MdcServletFilter`, `MdcHelper`, `MdcLogConstants`, `PerformanceLogging`, and `PerformanceLoggingAdvice` — these
  are local to the project and work with any Logback setup. Register `MdcServletFilter` as a `FilterRegistrationBean` in the Spring Boot
  config.
- The local `LogMarkers` class in the `logging` module (replace any imports of `se.inera.intyg.infra.monitoring.logging.LogMarkers` with the
  local version).

---

## 4. Migration Order

The work items have dependencies. The recommended execution order is:

```
┌─────────────────────────────────────────────────────┐
│ Phase 1: Foundation                                 │
│                                                     │
│  3.1  Spring Boot bootstrap                         │
│  3.2  Eliminate XML configuration                   │
│  3.4  Auto-configuration replacements               │
│       (JPA, JMS, Metrics, Redis)                    │
└──────────────────────┬──────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────┐
│ Phase 2: API & Dependencies                         │
│                                                     │
│  3.3  JAX-RS → Spring MVC                           │
│  3.5  Remove se.inera.intyg.infra dependencies      │
│  3.7  Spring Boot ECS logging                        │
└──────────────────────┬──────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────┐
│ Phase 3: Test Modernization                         │
│                                                     │
│  3.6  Migrate all tests to JUnit Jupiter            │
└─────────────────────────────────────────────────────┘
```

**Rationale:** Spring Boot must be in place before auto-configuration can replace manual beans. Infra removal and REST migration can then
proceed in parallel. Test migration is independent but benefits from having `@SpringBootTest` available.

---

## 5. Verification Criteria

The migration is complete when:

- [ ] The application starts as a Spring Boot executable JAR (`java -jar intygstjanst.jar`).
- [ ] No XML files are used for Spring bean configuration (data XML files like Liquibase changelogs and XSLT transforms are fine).
- [ ] All REST endpoints respond correctly using Spring MVC (`@RestController`).
- [ ] All SOAP endpoints respond correctly via CXF (configured in Java).
- [ ] `grep -r "se.inera.intyg.infra" build.gradle` returns no results.
- [ ] `grep -r "import org.junit\." --include="*.java" src/ | grep -v jupiter` returns no results.
- [ ] `junit-vintage-engine` is not in any `build.gradle`.
- [ ] `junit:junit` is not in any `build.gradle`.
- [ ] Structured ECS JSON logs are produced when `logging.structured.format.console=ecs` is set.
- [ ] Spring Boot Actuator health endpoint responds at `/actuator/health`.
- [ ] Prometheus metrics are available at `/actuator/prometheus`.
- [ ] All existing tests pass.
- [ ] The Docker image builds and runs successfully with the new JAR packaging.

---

## 6. Out of Scope

The following items from the [goal tech stack](goal-tech-stack.md) are **intentionally deferred** to later migrations:

| Item                                         | Reason for Deferral                                               |
|----------------------------------------------|-------------------------------------------------------------------|
| Hexagonal/domain module architecture         | Requires significant refactoring beyond the Spring Boot migration |
| Gradle Kotlin DSL migration                  | Independent concern; can be done separately                       |
| WSDL2Java plugin for code generation         | Independent concern; existing generated code works                |
| Testcontainers (MySQL, ActiveMQ, MockServer) | Can be adopted after Spring Boot is in place                      |
| MapStruct for DTO mapping                    | Additive improvement; not blocking                                |
| Awaitility for async testing                 | Additive improvement; not blocking                                |
| `se.inera.intyg.common` dependency reduction | Core business dependency; requires separate analysis              |
| ANTLR ST4 removal                            | Requires usage analysis                                           |
| Commons IO removal                           | Requires usage analysis                                           |
| ShedLock evaluation                          | Functional decision; retain for now                               |
| Spring Boot Starter WebFlux                  | Only needed if reactive HTTP clients are adopted                  |

---

## 7. Risk Assessment

| Risk                                               | Likelihood | Impact | Mitigation                                                                             |
|----------------------------------------------------|------------|--------|----------------------------------------------------------------------------------------|
| CXF + Spring Boot compatibility issues             | Medium     | High   | CXF has documented Spring Boot support; test early with a spike                        |
| Inlined infra code diverges from upstream          | Low        | Low    | This is intentional — we are decoupling from infra                                     |
| `se.inera.intyg.common` modules expect XML config  | Medium     | Medium | Test each common module's Spring context requirements; provide Java config equivalents |
| JUnit 4 → 5 migration breaks test behavior         | Low        | Medium | Mechanical migration; run full test suite after each batch                             |
| Spring Boot auto-config conflicts with CXF servlet | Medium     | Medium | Configure CXF servlet path explicitly to avoid clash with DispatcherServlet            |
| Property name changes (Spring Boot conventions)    | Medium     | Low    | Create a property mapping and update deployment configurations                         |

