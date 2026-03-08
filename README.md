# Intygstjänsten

## Tech Stack

- **Java** 21
- **Gradle** 8.14
- **Spring Boot** (Web, Data JPA, Actuator, ActiveMQ, Data Redis)
- **MySQL** (with Liquibase for database migrations)
- **Redis** (caching)
- **ActiveMQ** (messaging)
- **Apache CXF** (SOAP/web services)
- **JUnit 5** + **Mockito** (testing)

## Run Locally

### Prerequisites

The following services must be available locally:

| Service  | Default address   | Dev credentials                 |
|----------|-------------------|---------------------------------|
| MySQL    | `localhost:3306`  | `intyg` / `intyg` (db: `intyg`) |
| ActiveMQ | `localhost:61616` | —                               |
| Redis    | `localhost:6379`  | —                               |

### Start the application

```bash
./gradlew appRun
```

Debug mode (JDWP on port 8880):

```bash
./gradlew appRunDebug
```

### Ports

| Port | Purpose                            |
|------|------------------------------------|
| 8080 | Application (`/inera-certificate`) |
| 8081 | Internal API                       |
| 8082 | Management / Actuator              |

Active Spring profiles in dev: `dev,bootstrap,testability-api,caching-enabled,it-fk-stub`

## Verify the Service

**Health check:**

```bash
curl http://localhost:8082/actuator/health
```

**Run tests:**

```bash
./gradlew test
```

**Code coverage report:**

```bash
./gradlew jacocoTestReport
# Report: app/build/jacocoHtml/index.html
```

<!-- TODO: Is there a smoke test or specific integration test command to run? -->

## Further Documentation

- [`migration/`](migration/) — Technical migration plans and analysis
- [`LICENSE.md`](LICENSE.md) — LGPL-3.0

<!-- TODO: Add links to:
  - Internal wiki / Confluence
  - Architecture documentation
  - API documentation (Swagger/OpenAPI)
  - Runbooks
  - Dashboards (Grafana, etc.)
-->
