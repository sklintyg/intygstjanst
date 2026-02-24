# Intygstjänst — Technical Stack Analysis

*Generated: 2025-02-21*

## Overview

**Intygstjänst** ("Certificate Service") is a Swedish healthcare certificate management backend service, part of the **Inera/SKL Intyg**
platform. It's a multi-module **Java 21** server-side application packaged as a **WAR** and deployed on **Tomcat 10**.

---

## Core Platform

| Aspect                 | Technology    | Version/Details                                         |
|------------------------|---------------|---------------------------------------------------------|
| **Language**           | Java          | **21** (class major version 65)                         |
| **Build System**       | Gradle        | **8.14.4** (via wrapper), multi-module, parallel builds |
| **Application Server** | Apache Tomcat | **10** (Jakarta EE namespace)                           |
| **Packaging**          | WAR           | Deployed at context path `/inera-certificate`           |
| **Containerization**   | Docker        | Dockerfile deploys WAR into Catalina                    |

---

## Framework Stack

### Web & Service Layer

- **Spring Framework** (spring-webmvc, spring-jms, spring-test) — Core application framework
- **Apache CXF** (cxf-rt-frontend-jaxws, cxf-rt-features-logging) — **SOAP/JAXWS** web services
- **Jakarta EE** APIs (jakarta.ws.rs, jakarta.servlet, jakarta.transaction, jakarta.persistence, jakarta.xml.ws) — Full Jakarta EE 10+
  migration (not javax)
- **Jackson** (jackson-jakarta-rs-json-provider) — JSON serialization for REST endpoints

### Persistence Layer

- **Spring Data JPA** — Repository abstraction
- **Hibernate ORM** (hibernate-core, hibernate-hikaricp) — JPA implementation
- **HikariCP** — Connection pooling
- **Liquibase** — Database schema migration
- **MySQL** (mysql-connector-j) — Production database
- **H2** — In-memory test database

### Messaging

- **Apache ActiveMQ** (activemq-spring) — JMS messaging via Spring JMS

### Caching / Scheduling

- **Redis** — Used for caching (profile `caching-enabled`) and distributed locking
- **ShedLock** (shedlock-spring, shedlock-provider-redis-spring) — Distributed scheduled task locking via Redis

### Monitoring & Observability

- **Prometheus** (simpleclient_servlet) — Metrics endpoint
- **Logback** with **Elastic ECS encoder** (logback-ecs-encoder) — Structured JSON logging for ELK/Elastic stack
- **SLF4J** (jul-to-slf4j) — Log bridging

### Code Generation & Utilities

- **Lombok** — Annotation-based boilerplate reduction
- **JAXB2 Basics** (jaxb2-basics) — XML/SOAP schema code generation
- **Guava** — General utility library
- **Commons IO** — File/IO utilities
- **ANTLR ST4** — String templating
- **AspectJ** — AOP (e.g., performance logging)

---

## Domain-Specific / Inera Ecosystem Dependencies

This is a significant part of the stack — the app relies heavily on internal Inera libraries:

| Category                                      | Libraries                                                                                                                               | Purpose                                                                                                       |
|-----------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------|
| **Inera Infra** (`infraVersion: 4.1.0`)       | certificate, hsa-integration-api, pu-integration-api, intyginfo, message, monitoring, security-filter, sjukfall-engine, testcertificate | Shared infrastructure: HSA (healthcare address registry), PU (person data), sick leave calculations, security |
| **Inera Common** (`commonVersion: 4.1.0`)     | af00213, ag7804, lisjp, luse, ts-bas, db, doi, fk7263, fk-parent, common-schemas/services/support, etc.                                 | Certificate type definitions (~15 different medical certificate types)                                        |
| **Schema Libraries**                          | clinicalprocess-healthcond-certificate, rehabilitation, insuranceprocess-healthreporting, schemas-contract                              | RIV-TA SOAP contract schemas (Swedish national interoperability standards)                                    |
| **Reference Data** (`refDataVersion: 2.0.22`) | refdata                                                                                                                                 | Code systems, ICD-10 codes, etc.                                                                              |
| **Inera BOM** (`intygBomVersion: 1.0.0.11`)   | platform, catalog                                                                                                                       | Centralized dependency version management                                                                     |

---

## Module Structure

| Module            | Purpose                                                               |
|-------------------|-----------------------------------------------------------------------|
| **`web`**         | Main WAR module — controllers, services, CXF endpoints, Spring config |
| **`persistence`** | JPA entities, repositories, Liquibase migrations                      |
| **`logging`**     | Cross-cutting logging concerns (MDC, performance logging, AOP)        |

---

## Testing Stack

- **JUnit 5** (Jupiter) — Primary test framework
- **JUnit 4** (via junit-vintage-engine) — Legacy test support
- **Mockito** (with dedicated Java agent) — Mocking
- **Spring Test** — Integration testing
- **XMLUnit** — XML assertion/comparison

---

## Quality & CI/CD

- **SonarQube** — Static code analysis (project: `intyg-intygstjanst`)
- **JaCoCo** — Code coverage
- **CycloneDX** — Software Bill of Materials (SBOM) generation
- **Jenkins** — CI/CD (Jenkins.properties present)

---

## Key Observations

1. **SOAP-heavy architecture** — Uses Apache CXF with RIV-TA schemas (Swedish national e-health interoperability standard). This is not a
   REST-first application.
2. **Jakarta EE migration completed** — All namespaces use `jakarta.*`, not `javax.*`, indicating a successful migration to Jakarta EE 10+.
3. **Heavy coupling to Inera ecosystem** — ~30+ internal Inera dependencies. This app cannot run independently; it's part of a larger
   microservices platform.
4. **Not Spring Boot** — This is a traditional Spring Framework + WAR deployment, not Spring Boot. Uses Gretty plugin for local Tomcat 10
   development.
5. **Dual protocol** — Has both SOAP (CXF/JAXWS) and REST (JAX-RS/Jackson) capabilities.

---

## Suggested Additional Analysis

1. **Dependency vulnerability scan** — Check the third-party libraries for known CVEs (CycloneDX BOM is already generated).
2. **Database schema review** — Analyze the Liquibase changelogs to understand the data model.
3. **API surface analysis** — Catalog the SOAP/REST endpoints exposed by the web module.
4. **Spring configuration deep-dive** — Understand the profiles (dev, bootstrap, testability-api, caching-enabled, it-fk-stub) and how they
   wire things differently.
5. **Inera BOM version catalog inspection** — Understand exactly which versions of Spring, Hibernate, CXF, etc. are being pulled in (they're
   managed centrally by the BOM).
6. **Code architecture analysis** — Review the package structure in `web/src/main/java` to understand the service layer, domain model, and
   integration patterns.

