# Intygstjänst — Goal Tech Stack

*Based on the established patterns in **certificate-service** and **intyg-proxy-service**.*

---

## Rationale

The target services (**certificate-service** and **intyg-proxy-service**) represent the modern standard for the Inera Intyg platform. This
document defines what intygstjänst's tech stack should look like after consolidation, derived purely from what those two services already
use.

---

## Core Platform

| Aspect               | Current (Intygstjänst)                    | Goal                                                            |
|----------------------|-------------------------------------------|-----------------------------------------------------------------|
| **Language**         | Java 21                                   | **Java 21** — no change                                         |
| **Framework**        | Spring Framework (no Boot)                | **Spring Boot** — align with both target services               |
| **Build System**     | Gradle (Groovy DSL)                       | **Gradle with Kotlin DSL version catalog** from centralized BOM |
| **Packaging**        | WAR deployed on external Tomcat 10        | **Executable JAR** with embedded server (Spring Boot)           |
| **Containerization** | Docker (WAR into Catalina base image)     | **Docker** (JAR-based Spring Boot image)                        |
| **Dependency Mgmt**  | Inera BOM (`se.inera.intyg.bom:platform`) | **Inera BOM** — no change (already aligned)                     |

---

## Web & API Layer

| Concern             | Current                                    | Goal                                                                         |
|---------------------|--------------------------------------------|------------------------------------------------------------------------------|
| **REST API**        | JAX-RS (jakarta.ws.rs) + Jackson           | **Spring Boot Starter Web** (Spring MVC) + Jackson                           |
| **SOAP Services**   | Apache CXF (cxf-rt-frontend-jaxws)         | **Apache CXF** — retained, consistent with both target services              |
| **SOAP Codegen**    | JAXB2 Basics (manual/pre-generated)        | **WSDL2Java** plugin (`com.yupzip.wsdl2java`) + JAXB Runtime                 |
| **Reactive Client** | Not used                                   | **Spring Boot Starter WebFlux** — for outbound HTTP integrations (as needed) |
| **Health/Metrics**  | Prometheus `simpleclient_servlet` (manual) | **Spring Boot Actuator** — standardized health checks, metrics, management   |

---

## Data & Persistence

| Concern              | Current                         | Goal                                                   |
|----------------------|---------------------------------|--------------------------------------------------------|
| **ORM**              | Spring Data JPA + Hibernate ORM | **Spring Data JPA** (via Spring Boot Starter Data JPA) |
| **Connection Pool**  | HikariCP (explicit dependency)  | **HikariCP** (auto-configured by Spring Boot)          |
| **Schema Migration** | Liquibase                       | **Liquibase** — no change                              |
| **Production DB**    | MySQL (mysql-connector-j)       | **MySQL** — no change                                  |
| **Test DB**          | H2 (in-memory)                  | **Testcontainers MySQL** — real database in tests      |

---

## Messaging

| Concern     | Current                    | Goal                                                           |
|-------------|----------------------------|----------------------------------------------------------------|
| **JMS**     | ActiveMQ (activemq-spring) | **Spring Boot Starter ActiveMQ** — auto-configured             |
| **Testing** | —                          | **Testcontainers ActiveMQ** — real broker in integration tests |

---

## Caching & Scheduling

| Concern              | Current                                          | Goal                                                                             |
|----------------------|--------------------------------------------------|----------------------------------------------------------------------------------|
| **Caching**          | Redis (manual config, profile `caching-enabled`) | **Spring Data Redis** (`spring-boot-starter-data-redis`) — auto-configured       |
| **Distributed Lock** | ShedLock (shedlock-spring + redis provider)      | **ShedLock** — retain if still needed (not present in target services; evaluate) |

> **Note:** ShedLock is used in intygstjänst but not in either target service. If the scheduled task locking requirement remains, ShedLock
> can be retained. If the scheduling pattern can be redesigned, consider removing it to fully align.

---

## Logging & Observability

| Concern             | Current                                     | Goal                                                                                                              |
|---------------------|---------------------------------------------|-------------------------------------------------------------------------------------------------------------------|
| **Logging Backend** | Logback + ECS encoder (logback-ecs-encoder) | **Logback with ECS structured format** (`logging.structured.format.console=ecs`) — Spring Boot native ECS support |
| **Log Bridging**    | SLF4J + jul-to-slf4j (manual)               | **SLF4J** (auto-configured by Spring Boot)                                                                        |
| **AOP Logging**     | AspectJ (custom PerformanceLoggingAdvice)   | **AspectJ** with `@PerformanceLogging` — same pattern as target services                                          |
| **MDC**             | Custom MdcServletFilter                     | **MDC** — aligned with target services' logging module pattern                                                    |
| **Metrics**         | Prometheus simpleclient_servlet             | **Spring Boot Actuator** (Micrometer) — standard metrics endpoint                                                 |

---

## XML / SOAP Integration

| Concern             | Current                | Goal                                                                           |
|---------------------|------------------------|--------------------------------------------------------------------------------|
| **SOAP Framework**  | Apache CXF             | **Apache CXF** — no change                                                     |
| **XML Binding**     | JAXB2 Basics           | **JAXB Runtime** (GlassFish) + **Jakarta XML WS**                              |
| **Code Generation** | Pre-generated / manual | **WSDL2Java plugin** (`com.yupzip.wsdl2java`) — build-time generation          |
| **XML Validation**  | —                      | **Helger Schematron** — if Schematron validation is needed (from cert-service) |

---

## Utilities & Developer Productivity

| Concern              | Current            | Goal                                                           |
|----------------------|--------------------|----------------------------------------------------------------|
| **Boilerplate**      | Lombok             | **Lombok** — no change                                         |
| **DTO Mapping**      | Manual             | **MapStruct** (with Lombok binding) — from intyg-proxy-service |
| **General Utils**    | Guava + Commons IO | **Guava** — retain; drop Commons IO if not needed              |
| **String Templates** | ANTLR ST4          | Evaluate necessity — not used in either target service         |

---

## Inera Ecosystem Dependencies

| Concern              | Current                               | Goal                                                                                                                        |
|----------------------|---------------------------------------|-----------------------------------------------------------------------------------------------------------------------------|
| **Inera Infra**      | ~10 modules (hsa, pu, sjukfall, etc.) | **Minimize** — use integration modules via intyg-proxy-service REST APIs instead of direct SOAP dependencies where possible |
| **Inera Common**     | ~15 certificate type modules          | **Retain as needed** — certificate type support is core business                                                            |
| **Schema Libraries** | RIV-TA schemas (multiple)             | **Retain** — required for SOAP interoperability                                                                             |
| **Reference Data**   | refdata                               | **Retain** — required for code systems                                                                                      |
| **Security**         | security-filter (Inera custom)        | Evaluate — target services handle security at infrastructure level                                                          |

---

## Testing Stack

| Concern               | Current                      | Goal                                                                          |
|-----------------------|------------------------------|-------------------------------------------------------------------------------|
| **Unit Testing**      | JUnit 5 (Jupiter)            | **JUnit 5** — no change                                                       |
| **Legacy Tests**      | JUnit 4 (via vintage engine) | **JUnit 5 only** — migrate remaining JUnit 4 tests                            |
| **Mocking**           | Mockito (Java agent)         | **Mockito** (Java agent) — no change                                          |
| **Integration Tests** | Spring Test + H2             | **Testcontainers** (MySQL, ActiveMQ, MockServer) — real dependencies in tests |
| **HTTP Mocking**      | —                            | **MockServer** and/or **OkHttp MockWebServer** — for external service mocking |
| **Async Testing**     | —                            | **Awaitility** — for async/JMS test assertions                                |
| **Contract Testing**  | —                            | **Microcks Testcontainers** — if SOAP contract testing is needed              |
| **XML Assertions**    | XMLUnit                      | **XMLUnit** — retain if XML comparison is still needed                        |
| **Spring Testing**    | Spring Test (manual context) | **Spring Boot Test** — auto-configured test context                           |

---

## Quality & CI/CD

| Concern         | Current   | Goal                                |
|-----------------|-----------|-------------------------------------|
| **Coverage**    | JaCoCo    | **JaCoCo** — no change              |
| **Analysis**    | SonarQube | **SonarQube** — no change           |
| **SBOM**        | CycloneDX | **CycloneDX** — no change           |
| **CI/CD**       | Jenkins   | **Jenkins** — no change             |
| **Dep Updates** | —         | **Ben Manes Versions plugin** — add |

---

## Module Architecture (Goal)

The target module structure should follow the patterns established in certificate-service (hexagonal/clean architecture with separated
domain) and intyg-proxy-service (dedicated integration modules):

```
intygstjanst (goal)
├── app                          → Spring Boot application (controllers, services, Spring config)
├── domain                       → Pure business logic (minimal deps: Jackson, SLF4J only)
├── logging                      → Cross-cutting logging/AOP (AspectJ, Logback) — already exists
├── persistence                  → JPA entities, repositories, Liquibase — already exists (fold into app or keep)
├── integration-*                → Dedicated modules for external service clients (if applicable)
└── integration-test             → Full integration tests with Testcontainers
```

**Key architectural shift:** Introduce a **domain module** with no framework dependencies (following certificate-service's hexagonal
pattern),
separating business logic from infrastructure concerns.

---

## Summary of Key Changes

| #  | Change                                                    | Impact |
|----|-----------------------------------------------------------|--------|
| 1  | **Spring Framework → Spring Boot**                        | Major  |
| 2  | **WAR + external Tomcat → executable JAR**                | Major  |
| 3  | **JAX-RS → Spring MVC** for REST endpoints                | Major  |
| 4  | **Prometheus manual → Spring Boot Actuator**              | Medium |
| 5  | **H2 test DB → Testcontainers**                           | Medium |
| 6  | **JUnit 4 vintage → JUnit 5 only**                        | Medium |
| 7  | **Manual Spring config → Spring Boot auto-configuration** | Medium |
| 8  | **Add MapStruct** for DTO mapping                         | Low    |
| 9  | **Add Awaitility** for async testing                      | Low    |
| 10 | **Add Ben Manes Versions plugin**                         | Low    |
| 11 | **Introduce domain module** (hexagonal architecture)      | Major  |
| 12 | **Docker image: WAR/Catalina → JAR/Spring Boot base**     | Medium |

---

## Items Requiring Decision

These items differ between the current state and the target services, or are unique to intygstjänst, and need a deliberate decision:

1. **ShedLock** — Keep or remove? Not used in either target service.
2. **ANTLR ST4** — Keep or remove? Not used in either target service.
3. **Commons IO** — Keep or replace with standard Java/Guava equivalents?
4. **Inera security-filter** — Keep, or handle security at infrastructure level like the target services?
5. **Inera Infra direct dependencies** (hsa-integration-api, pu-integration-api) — Keep direct integration, or call via intyg-proxy-service
   REST APIs?
6. **PDF generation** — If needed, adopt Apache PDFBox (from certificate-service).
7. **Redis usage** — Align with intyg-proxy-service's Spring Data Redis pattern, or simplify if caching needs differ.

